package networkoperations;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import nodeoperations.PeerConnection; 
import nodeoperations.Handshake; 
import nodeoperations.BitField; 
import nodeoperations.PeerProcess;
import nodeoperations.FileDownloadStatus;
import nodeoperations.OutMessage;
import nodeoperations.Choke;
import nodeoperations.Unchoke;
import fileparsers.StatusLogger;


//Class will enable PeerConnection to function as a Server
public class PeerServer extends Thread{

    private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ0000000000";
    private static final int HANDSHAKE_HEADER_LENGTH = 28;
    private static final int PEER_ID_LENGTH = 4;

    private int self_ID; 
    private int totalChunks; 
    private boolean allChunksPresent; 
    private int portno; 
    private int fileSize;
    private int chunkSize; 
    private int peerId;

    public PeerServer(int self_ID, int totalChunks, boolean allChunksPresent, int portno, long fileSize, long chunkSize){
        this.self_ID = self_ID;
        this.totalChunks = totalChunks;
        this.allChunksPresent = allChunksPresent;
        this.portno = portno;
        this.fileSize = (int) fileSize;
        this.chunkSize = (int) chunkSize;
    }

    //below method creates new thread upon .start() invocation from main process
    @Override
    public void run(){


        try (ServerSocket server = new ServerSocket(portno)) {
            //server connectionSocket created to listen for incoming connections on specified portno.
            new Thread(new ChokeStatusUpdater()).start();
            //Server needs to keep accepting connections
            while(true){
                try{
                    Socket peerSocket = server.accept();
                    boolean canContinue = PerformHandshake(peerSocket);

                    if(canContinue)
                    {

                        PeerConnection p = new PeerConnection(self_ID, peerId, peerSocket);

                        try{
                            exchangeBitfields(peerSocket,p);
                        } catch(IOException e){
                            System.err.println(e);
                        }

                        System.out.println("Finished exchange of Bit fields and Handshake");
                        PeerProcess.peers.add(p);
                        StatusLogger.madeTCPConnection(peerId);

                        FileDownloadStatus completeFile = new FileDownloadStatus();
                        completeFile.setConnectionSocket(peerSocket);
                        completeFile.setFullFileDownloadComplete(false);

                          PeerProcess.hasDownloadedFullFile.add(completeFile);

                        startCommunicationThreads(peerId, peerSocket);
                    }
                    else{
                        System.out.println("Not a valid peer");
                    }
                }catch(IOException err){
                    System.err.println(err);
                }
            }
        }catch(IOException err){
            System.err.println(err);
        }
        
    }

    private boolean PerformHandshake(Socket peer){

        //Handshake Reception Logic
        ByteBuffer receivedHandshake  = null;

        try {
            ObjectInputStream in = new ObjectInputStream(peer.getInputStream());
            receivedHandshake  = ByteBuffer.wrap((byte[]) in.readObject());
        } catch (IOException e) {
            System.err.println(e);
        } catch (ClassNotFoundException err) {
            System.err.println(err);
        }

        //Handshake Sending
        try {
            ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
            out.writeObject(new Handshake(self_ID).handshake);
        } catch (IOException e) {
            System.err.println(e);
        }


        byte[] headerBytes = new byte[HANDSHAKE_HEADER_LENGTH];
        receivedHandshake.get(headerBytes, 0, HANDSHAKE_HEADER_LENGTH);
        String header = new String(headerBytes);

        byte[] peerIdBytes = new byte[PEER_ID_LENGTH];
        receivedHandshake.get(peerIdBytes, 0, PEER_ID_LENGTH);
        String peerIdStr = new String(peerIdBytes).trim();
        peerId = Integer.parseInt(peerIdStr);  

        boolean authorized = false;

        if(header.equals(HANDSHAKE_HEADER))
        {
            for (Integer currentId : PeerProcess.allPeerIDs) { 
                int curr_value = currentId.intValue();

                if(peerId == curr_value)
                {
                    authorized = true;
                    break;
                }
            }
        }

        return authorized;

    }

    private void startCommunicationThreads(int peerId, Socket connectionSocket) {
          new OutboundMessageHandler().start();
          new RequestHandler(peerId, totalChunks, allChunksPresent, fileSize, chunkSize).start();
          new IncomingMessageHandler(connectionSocket, chunkSize).start();
    }

    private void exchangeBitfields(Socket peerSocket, PeerConnection peer) throws IOException {
        ObjectOutputStream out = null;
        try{
            out = new ObjectOutputStream(peerSocket.getOutputStream());
            out.flush();
            // Server peer's bitfield
            out.writeObject(BitField.bitfield);
            // Receive client peer's bitfield
            ObjectInputStream in = null;
            in = new ObjectInputStream(peerSocket.getInputStream());
            peer.setPeerBitfield((byte[]) in.readObject());  
            peer.setPeerInterested(false);  
        }catch (Exception e) {
            throw new IOException("Invalid bitfield format", e);
        }     
    }

    // Global thread that updates the choke status for peers
    private class ChokeStatusUpdater implements Runnable {
        private static final int K = 500; 

        @Override
        public void run() {
            Random rand = new Random();
            int j=0;
            while (true) {
                try {
                    // Wait for 'K' milliseconds
                    Thread.sleep(K);

                    // Find all interested peers
                    ArrayList<PeerConnection> interestedPeers = new ArrayList<>();
                    synchronized (PeerProcess.peers)
                    {
                        for (PeerConnection peer : PeerProcess.peers) {
                            if (peer.isRemotePeerInterested()) {
                                interestedPeers.add(peer);
                            }
                        }

                        if (interestedPeers.size() > 0) {

                            int k = 2;

                            ArrayList<PeerConnection> selectedPeers = new ArrayList<>();
                            for (int i = 0; i < interestedPeers.size(); i++){
                                selectedPeers.add(interestedPeers.get(i));
                               }
                        // Set choke status
                        for (PeerConnection peer : PeerProcess.peers) {
                            if (selectedPeers.contains(peer)) {
                                peer.setChoked(false);
                                synchronized (PeerProcess.outgoingMessageQueue) {
                                    OutMessage messageToSend = new OutMessage();
                                    messageToSend.setConnectionSocket(peer.getConnectionSocket());
                                    messageToSend.setMessage((new Unchoke()).unchoke);
                                    PeerProcess.outgoingMessageQueue.add(messageToSend);
                                }
                            } else {
                                peer.setChoked(true);
                                synchronized (PeerProcess.outgoingMessageQueue) {
                                    OutMessage messageToSend = new OutMessage();
                                    messageToSend.setConnectionSocket(peer.getConnectionSocket());
                                    messageToSend.setMessage((new Choke()).choke);
                                    PeerProcess.outgoingMessageQueue.add(messageToSend);
                                }
                            }
                        }


                        }
                    }

                } catch (InterruptedException e) {
                    System.err.println("ChokeStatusUpdater interrupted: " + e.getMessage());
                }
            }
        }
    }


}