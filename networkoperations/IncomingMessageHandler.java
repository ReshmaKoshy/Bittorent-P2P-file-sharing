package networkoperations;

import java.io.ObjectInputStream;
import java.net.Socket;
import nodeoperations.Piece;
import nodeoperations.PeerProcess;
import java.nio.ByteBuffer;
import nodeoperations.PeerConnection;
import java.util.ListIterator;
import nodeoperations.OutMessage;
import nodeoperations.Have;
import nodeoperations.BitField;
import nodeoperations.FileDownloadStatus;
import fileparsers.StatusLogger;


public class IncomingMessageHandler extends Thread {

    private Socket connectionSocket;
    private int peerIdentifier;
    private long fragmentSize;
    private PeerConnection currentPeer;

    public IncomingMessageHandler(Socket connectionSocket, long fragmentSize) {
        this.connectionSocket = connectionSocket;
        this.fragmentSize = fragmentSize;

        ListIterator<PeerConnection> iterator = PeerProcess.peers.listIterator();

        while(iterator.hasNext()) {
            currentPeer = (PeerConnection)iterator.next();

            if(currentPeer.getConnectionSocket().equals(connectionSocket)) {
                peerIdentifier = currentPeer.getRemotePeerId();
            }
        }
    }

    @Override
    public void run() {

        while(true) {

            byte[] incomingMessage = retrieveIncomingMessage();
            int messageType = incomingMessage[4];

            switch(messageType) {
                case 0: // choke
                    synchronized (PeerProcess.peers)
                    {
                        currentPeer.setChoked(true);
                    }

                    System.out.println("This Peer is choked by" + peerIdentifier);
                    System.out.println();
                    StatusLogger.logChoke(peerIdentifier);
                    break;
                case 1: // unchoke
                    synchronized (PeerProcess.peers)
                    {
                        currentPeer.setChoked(false);
                    }

                    System.out.println("This Peer is unchoked by" + peerIdentifier);
                    System.out.println();
                    StatusLogger.logUnchoke(peerIdentifier);
                    break;
                case 2: // interested
                    System.out.println("Interested message received from " + peerIdentifier);
                    System.out.println();
                    synchronized (PeerProcess.peers)
                    {
                        currentPeer.setRemotePeerInterested(true);
                    }

                    StatusLogger.receiveInterested(peerIdentifier);
                    break;

                case 3: // not interested
                    System.out.println("Not Interested message received from " + peerIdentifier);
                    System.out.println();
                    StatusLogger.receiveNotInterested(peerIdentifier);
                    break;

                case 4: // have
                    byte[] pieceBytes = new byte[4];

                    int byteOffset = 5;
                    for (int i = 0; i < pieceBytes.length; i++) {
                        pieceBytes[i] = incomingMessage[byteOffset];
                        byteOffset++;
                    }

                    int pieceNumber = ByteBuffer.wrap(pieceBytes).getInt();

                    ListIterator<PeerConnection> peerIterator = PeerProcess.peers.listIterator();

                    while(peerIterator.hasNext()) {
                        PeerConnection targetPeer = (PeerConnection)peerIterator.next();

                        if(targetPeer.getConnectionSocket().equals(connectionSocket)) {
                            byte[] peerBitfield = targetPeer.getPeerBitfield();

                            try {
                                synchronized(peerBitfield) {
                                    peerBitfield = bitFieldUpdate(peerBitfield, pieceNumber);
                                    targetPeer.setPeerBitfield(peerBitfield);
                                }
                            } catch (Exception e) {
                                System.err.println(e);
                            }
                        }
                    }

                    System.out.println("Have message received from " + peerIdentifier + " for piece " + pieceNumber);
                    System.out.println();
                    StatusLogger.receiveHave(peerIdentifier, pieceNumber);
                    break;

                case 6: // request
                    byte[] requestBytes = new byte[4];

                    byteOffset = 5;
                    for (int i = 0; i < requestBytes.length; i++) {
                        requestBytes[i] = incomingMessage[byteOffset];
                        byteOffset++;
                    }
                    int requestedPieceNum = ByteBuffer.wrap(requestBytes).getInt();
                    Integer requestedPieceIndex = Integer.valueOf(requestedPieceNum);

                    Piece requestedPiece = PeerProcess.chunkIndexPieceMap.get(requestedPieceIndex);

                    System.out.println("piece " + requestedPieceNum + " requested from " + peerIdentifier);
                    System.out.println();

                    synchronized (PeerProcess.outgoingMessageQueue) {
                        OutMessage messageToSend = new OutMessage();
                        messageToSend.setConnectionSocket(connectionSocket);
                        messageToSend.setMessage(requestedPiece.piece);
                        PeerProcess.outgoingMessageQueue.add(messageToSend);
                    }
                    break;

                case 7: // piece
                    byte[] indexBytes = new byte[4];

                    byteOffset = 5;
                    for (int i = 0; i < indexBytes.length; i++) {
                        indexBytes[i] = incomingMessage[byteOffset];
                        byteOffset++;
                    }
                    int pieceIndex = ByteBuffer.wrap(indexBytes).getInt();
                    Integer pieceNum = Integer.valueOf(pieceIndex);
                    byte[] pieceContent = new byte[incomingMessage.length - 9];
                    for (int i = 0; i < pieceContent.length; i++) {
                        pieceContent[i] = incomingMessage[byteOffset];
                        byteOffset++;
                    }

                    if(pieceContent.length == fragmentSize && !PeerProcess.chunkIndexPieceMap.containsKey(pieceNum)) {

                        Piece newPiece = new Piece(pieceIndex, pieceContent);

                        try {
                            synchronized(PeerProcess.chunkIndexPieceMap) {
                                PeerProcess.chunkIndexPieceMap.put(pieceNum, newPiece);
                                Thread.sleep(30);
                            }
                        } catch (Exception e) {
                            System.err.println(e);
                        }

                        System.out.println("piece " + pieceIndex + " received from " + peerIdentifier);
                        System.out.println();
                        StatusLogger.downloadPiece(peerIdentifier, pieceIndex);

                        try {
                            synchronized(BitField.bitfield) {
                                BitField.modifyBitField(pieceIndex);
                                Thread.sleep(20);
                            }
                        }
                        catch (InterruptedException e) {
                            System.err.println(e);
                        }

                        Have haveMessage = new Have(pieceIndex);

                        ListIterator<PeerConnection> broadcastIterator = PeerProcess.peers.listIterator();

                        while(broadcastIterator.hasNext()) {
                            PeerConnection broadcastPeer = (PeerConnection)broadcastIterator.next();

                            synchronized (PeerProcess.outgoingMessageQueue) {
                                OutMessage broadcastMessage = new OutMessage();
                                broadcastMessage.setConnectionSocket(broadcastPeer.getConnectionSocket());
                                broadcastMessage.setMessage(haveMessage.have);
                                PeerProcess.outgoingMessageQueue.add(broadcastMessage);
                            }
                        }
                    }
                    break;

                case 8:
                    synchronized(PeerProcess.hasDownloadedFullFile) {

                        ListIterator<FileDownloadStatus> completeFileIterator = PeerProcess.hasDownloadedFullFile.listIterator();

                        while(completeFileIterator.hasNext()) {
                            FileDownloadStatus completePeer = (FileDownloadStatus)completeFileIterator.next();

                            if(completePeer.getConnectionSocket().equals(connectionSocket)) {
                                completePeer.setFullFileDownloadComplete(true);
                                break;
                            }
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            System.err.println(e);
                        }
                    }
                    break;
            }
        }
    }

    private byte[] retrieveIncomingMessage() {

        byte[] message = null;
        try {
            ObjectInputStream inputStream = new ObjectInputStream(connectionSocket.getInputStream());
            message = (byte[]) inputStream.readObject();
        }

        catch (Exception e) {
            System.err.println(e);
            System.exit(0);
        }
        return message;
    }

    public byte[] bitFieldUpdate(byte[] field, int pieceIndex) {
        int bytePosition = (pieceIndex - 1) / 8;
        int bitPosition = 7 - ((pieceIndex - 1) % 8);
        field[bytePosition + 5] = (byte) (field[bytePosition + 5] | (1<<bitPosition));
        return field;
    }
}