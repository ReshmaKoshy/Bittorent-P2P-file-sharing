package nodeoperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

//import nodeoperations.BitField;
//import nodeoperations.Piece;
//import nodeoperations.OutMessage;
import networkoperations.PeerServer;
import networkoperations.PeerConnectionHandler;
import fileparsers.PeerConfigurationReader;
import fileparsers.FileChunkReader;
import fileparsers.PeerInfoFileParse;
import fileparsers.StatusLogger;

public class PeerProcess {

    // Configuration and state variables
    private long totalFileSize;
    private long chunkSize;
    private int totalPieces;
    private int peerID;
    private int port;
    private boolean hasFullFile;
    public static String targetFileName;

    // Static collections for peers and pieces
    public static ArrayList<PeerConnection> peers = new ArrayList<PeerConnection>();
    public static ArrayList<Integer> allPeerIDs;
    public static Map<Integer, Piece> chunkIndexPieceMap = new HashMap<>();
    
    public static LinkedList<OutMessage> outgoingMessageQueue = new LinkedList<OutMessage>();
    public static ArrayList<FileDownloadStatus> hasDownloadedFullFile = new ArrayList<FileDownloadStatus>();
    
    public static void main(String[] args) {
        PeerProcess peerProcess = new PeerProcess();
        peerProcess.initialize(args);
    }

    private void initialize(String[] args) {
        setPeerCommonConfiguration();
        setPeerInfo(Integer.parseInt(args[0]));
        setBitField();

        if (hasFullFile) {
            breakFileIntoChunks();
            startServer();
        } else {
            startServer();
            startClient();
        }
    }

    private void setPeerCommonConfiguration() {
        PeerConfigurationReader configReader = new PeerConfigurationReader();
        configReader.loadConfiguration();

        targetFileName = configReader.getTargetFileName();
        totalFileSize = configReader.getTotalFileSize();
        chunkSize = configReader.getChunkSize();
        totalPieces = (int) Math.ceil((double) totalFileSize / chunkSize);
    }

    private void setPeerInfo(Integer peerId) {
        PeerInfoFileParse peerInfo = new PeerInfoFileParse(peerId);
        peerInfo.setPeerValues();
        allPeerIDs = peerInfo.getFullPeerIds();

        peerID = peerInfo.getOwnPeerId();
        port = peerInfo.getPort();
        hasFullFile = peerInfo.isFullFile();

        StatusLogger.startLogger(peerId);
    }

    private void setBitField() {
        BitField.initializeBitfield(hasFullFile, totalPieces);
    }

    private void breakFileIntoChunks(){
        FileChunkReader chunkReader = new FileChunkReader(peerID, chunkSize, targetFileName);
        chunkIndexPieceMap = chunkReader.processFile();
    }


    private void startServer() {
        PeerServer peerServer = new PeerServer(peerID, totalPieces, hasFullFile, port, totalFileSize, chunkSize);
        peerServer.start();
    }

    private void startClient() {
        PeerConnectionHandler connectionHandler = new PeerConnectionHandler(peerID, totalPieces, hasFullFile, totalFileSize, chunkSize);
        connectionHandler.start();
    }
}
