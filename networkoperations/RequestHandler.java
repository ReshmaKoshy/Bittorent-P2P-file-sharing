package networkoperations;

import java.net.Socket;
import java.util.ListIterator;
import java.util.Random;

import nodeoperations.FileDownloadStatus;
import nodeoperations.OutMessage;
import nodeoperations.PeerConnection;
import nodeoperations.PeerProcess;
import nodeoperations.BitField;
import nodeoperations.Interested;
import nodeoperations.NotInterested;
import nodeoperations.Request;
import fileparsers.StatusLogger;
import fileparsers.MergeFile;

public class RequestHandler extends Thread {

    private int targetPeerId;
    private int currentPeerId;
    private int totalPieceCount;
    private boolean hasAllPieces;
    private long totalFileSize;
    private long singlePieceSize;
    private int interestFlag = 0;
    Socket connectionSocket;

    public RequestHandler(int targetPeerId, int totalPieceCount, boolean hasAllPieces, long totalFileSize, long singlePieceSize) {
        this.targetPeerId = targetPeerId;
        this.totalPieceCount = totalPieceCount;
        this.hasAllPieces = hasAllPieces;
        this.totalFileSize = totalFileSize;
        this.singlePieceSize = singlePieceSize;
    }

    @Override
    public void run() {
        if(!hasAllPieces) {

            PeerConnection targetPeer = null;
            byte[] peerBitfield;
            int selectedPieceIndex;

            synchronized(PeerProcess.peers) {
                ListIterator<PeerConnection> peerIterator = PeerProcess.peers.listIterator();

                while(peerIterator.hasNext()) {
                    targetPeer = peerIterator.next();

                    if(targetPeer.getRemotePeerId() == targetPeerId) {
                        currentPeerId = targetPeer.getLocalPeerId();
                        connectionSocket = targetPeer.getConnectionSocket();
                        break;
                    }
                }
            }

            while(true) {

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    System.err.println(e);
                }

                boolean isFileDownloadComplete = FileDownloadStatus();

                if(isFileDownloadComplete) {
                    if(!StatusLogger.fileFlag) {
                        StatusLogger.fileFlag = true;

                        System.out.println("Download complete");
                        StatusLogger.downloadComplete();

                        MergeFile fileAssembler = new MergeFile();
                        fileAssembler.reassemble(totalPieceCount, currentPeerId, totalFileSize, singlePieceSize);

                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            System.err.println(e);
                        }
                    }

                    break;
                }

                else {

                    if(targetPeer.isRemotePeerInteresting()) {
                        peerBitfield = targetPeer.getPeerBitfield();
                        selectedPieceIndex = getPieceInfo(peerBitfield, BitField.bitfield);
                        if(selectedPieceIndex == 0) {
                            targetPeer.setPeerInterested(false);
                            NotInterested notInterestedMessage = new NotInterested();
                            synchronized (PeerProcess.outgoingMessageQueue) {
                                OutMessage message = new OutMessage();
                                message.setConnectionSocket(connectionSocket);
                                message.setMessage(notInterestedMessage.not_interested);
                                PeerProcess.outgoingMessageQueue.add(message);
                            }
                            interestFlag = 1;
                        }

                        else if(!targetPeer.isChoked()) {
                            Request pieceRequest = new Request(selectedPieceIndex);
                            synchronized (PeerProcess.outgoingMessageQueue) {
                                OutMessage message = new OutMessage();
                                message.setConnectionSocket(connectionSocket);
                                message.setMessage(pieceRequest.request);
                                PeerProcess.outgoingMessageQueue.add(message);
                            }
                        }
                    }

                    else {

                        peerBitfield = targetPeer.getPeerBitfield();
                        selectedPieceIndex = getPieceInfo(peerBitfield, BitField.bitfield);

                        if(selectedPieceIndex == 0) {
                            if(interestFlag == 0) {
                                NotInterested notInterestedMessage = new NotInterested();

                                synchronized (PeerProcess.outgoingMessageQueue) {
                                    OutMessage message = new OutMessage();
                                    message.setConnectionSocket(connectionSocket);
                                    message.setMessage(notInterestedMessage.not_interested);
                                    PeerProcess.outgoingMessageQueue.add(message);
                                }
                            }
                        }

                        else {
                            targetPeer.setPeerInterested(true);
                            interestFlag = 0;

                            Interested interestedMessage = new Interested();

                            synchronized (PeerProcess.outgoingMessageQueue) {
                                OutMessage message = new OutMessage();
                                message.setConnectionSocket(connectionSocket);
                                message.setMessage(interestedMessage.interested);
                                PeerProcess.outgoingMessageQueue.add(message);
                            }

                            if(!targetPeer.isChoked()) {
                                Request pieceRequest = new Request(selectedPieceIndex);
                                synchronized (PeerProcess.outgoingMessageQueue) {
                                    OutMessage message = new OutMessage();
                                    message.setConnectionSocket(connectionSocket);
                                    message.setMessage(pieceRequest.request);
                                    PeerProcess.outgoingMessageQueue.add(message);
                                }
                            }
                        }
                    }
                }
            }
        }

        byte[] downloadCompleteNotification = new byte[5];

        for (int i = 0; i < downloadCompleteNotification.length - 1; i++) {
            downloadCompleteNotification[i] = 0;
        }
        downloadCompleteNotification[4] = 8;

        broadcastDownloadComplete(downloadCompleteNotification);

        while(true) {
            boolean allPeersDownloaded = checkAllPeerFileDownloaded();

            if(allPeersDownloaded)
            {
                System.out.println("All peers downloaded");
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                System.err.println(e);
            }

            if(allPeersDownloaded && PeerProcess.outgoingMessageQueue.isEmpty())
                break;
        }

        if(!StatusLogger.fileCompleteFlag) {
            StatusLogger.fileCompleteFlag = true;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e);
            }

            StatusLogger.closeLogger();
        }

        System.exit(0);
    }

    private boolean checkAllPeerFileDownloaded() {
        boolean allDownloaded = true;

        ListIterator<FileDownloadStatus> peerIterator = PeerProcess.hasDownloadedFullFile.listIterator();

        while(peerIterator.hasNext()) {
            FileDownloadStatus peer = peerIterator.next();
            if(!peer.isFullFileDownloadComplete()) {
                allDownloaded = false;
                break;
            }
        }

        return allDownloaded;
    }

    private void broadcastDownloadComplete(byte[] downloadCompleteNotification) {
        ListIterator<FileDownloadStatus> peerIterator = PeerProcess.hasDownloadedFullFile.listIterator();

        while(peerIterator.hasNext()) {
            FileDownloadStatus peer = peerIterator.next();

            synchronized (PeerProcess.outgoingMessageQueue) {
                OutMessage message = new OutMessage();
                message.setConnectionSocket(peer.getConnectionSocket());
                message.setMessage(downloadCompleteNotification);
                PeerProcess.outgoingMessageQueue.add(message);
            }
        }
    }

    private boolean FileDownloadStatus() {
        int completionFlag = 1;

        byte[] field = BitField.bitfield;

        for (int i = 5; i < field.length - 1; i++) {
            if(field[i] != -1) {
                completionFlag = 0;
                break;
            }
        }

        if(completionFlag == 1) {

            int remainingPieces = totalPieceCount % 8;
            int lastByte = field[field.length - 1];
            String lastByteBinary = Integer.toBinaryString(lastByte & 255 | 256).substring(1);
            char[] binaryChars = lastByteBinary.toCharArray();
            int[] binaryDigits = new int[8];

            for (int j = 0; j < binaryChars.length; j++) {
                binaryDigits[j] = binaryChars[j] - 48;
            }

            for (int j = 0; j < remainingPieces; j++) {
                if(binaryDigits[j] == 0) {
                    completionFlag = 0;
                    break;
                }
            }
        }

        return (completionFlag == 1);
    }

    private int getPieceInfo(byte[] peerField, byte[] localBitfield) {
        int[] pieceStatus = new int[totalPieceCount];
        int pieceStatusIndex = 0;
        int totalMissingPieces = 0;
        int remainingPieces  = totalPieceCount % 8;

        for (int byteIndex = 5; byteIndex < localBitfield.length; byteIndex++) {

            int localByte = localBitfield[byteIndex];
            int peerByte = peerField[byteIndex];

            String localByteBinary = Integer.toBinaryString(localByte & 255 | 256).substring(1);
            char[] localBinaryChars = localByteBinary.toCharArray();
            int[] localBinaryDigits = new int[8];

            for (int j = 0; j < localBinaryChars.length; j++) {
                localBinaryDigits[j] = localBinaryChars[j] - 48;
            }

            String peerByteBinary = Integer.toBinaryString(peerByte & 255 | 256).substring(1);
            char[] peerBinaryChars = peerByteBinary.toCharArray();
            int[] peerBinaryDigits = new int[8];

            for (int j = 0; j < peerBinaryChars.length; j++) {
                peerBinaryDigits[j] = peerBinaryChars[j] - 48;
            }

            if(byteIndex < localBitfield.length - 1) {

                for (int bitIndex = 0; bitIndex < peerBinaryDigits.length; bitIndex++) {
                    if(localBinaryDigits[bitIndex] == 0 && peerBinaryDigits[bitIndex] == 1) {
                        pieceStatus[pieceStatusIndex] = 0;
                        pieceStatusIndex++;
                        totalMissingPieces++;
                    }

                    if(localBinaryDigits[bitIndex] == 0 && peerBinaryDigits[bitIndex] == 0) {
                        pieceStatus[pieceStatusIndex] = 1;
                        pieceStatusIndex++;
                    }

                    if(localBinaryDigits[bitIndex] == 1) {
                        pieceStatus[pieceStatusIndex] = 1;
                        pieceStatusIndex++;
                    }
                }
            }

            else {
                for (int bitIndex = 0; bitIndex < remainingPieces; bitIndex++) {
                    if(localBinaryDigits[bitIndex] == 0 && peerBinaryDigits[bitIndex] == 1) {
                        pieceStatus[pieceStatusIndex] = 0;
                        pieceStatusIndex++;
                        totalMissingPieces++;
                    }

                    if(localBinaryDigits[bitIndex] == 0 && peerBinaryDigits[bitIndex] == 0) {
                        pieceStatus[pieceStatusIndex] = 1;
                        pieceStatusIndex++;
                    }

                    if(localBinaryDigits[bitIndex] == 1) {
                        pieceStatus[pieceStatusIndex] = 1;
                        pieceStatusIndex++;
                    }
                }
            }

        }

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        if(totalMissingPieces == 0)
            return 0;

        int[] selectablePieces = new int[totalMissingPieces];

        int selectablePiecesIndex = 0;
        for (int pieceIndex = 0; pieceIndex < pieceStatus.length; pieceIndex++) {
            if(pieceStatus[pieceIndex] == 0) {
                selectablePieces[selectablePiecesIndex] = pieceIndex;
                selectablePiecesIndex++;
            }
        }

        int randomPieceIndex = selectRandomPiece(totalMissingPieces);
        int selectedPiece = selectablePieces[randomPieceIndex];

        return (selectedPiece + 1);
    }

    private int selectRandomPiece(int totalMissingPieces) {
        Random random = new Random();
        int randomIndex = random.nextInt(totalMissingPieces);

        return randomIndex;
    }


}