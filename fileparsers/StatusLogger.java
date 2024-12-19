package fileparsers;

import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.LinkedList;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.Date;

public class StatusLogger {

    private static int myPeerID;
    private static File logFile;
    private static BufferedWriter logWriter;
    private static int downloadedPieceCount = 0;
    public static boolean fileFlag = false;
    public static boolean fileCompleteFlag = false;
    public static LinkedList fileWriteOperation = new LinkedList();

    public static void startLogger(int peerID) {

        myPeerID = peerID;
        String fileName = (new File(System.getProperty("user.dir")) + "/log_peer_" + myPeerID + ".log");

        logFile = new File(fileName);

        try {
            logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)));
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
    }

    public static void makeTCPConnection(int peerID) {

        try {
            String date = new Date().toString();
            String logMessage = date + " : Peer " + myPeerID + " makes a connection to Peer " + peerID + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void madeTCPConnection(int peerID) {

        try {
            String date = new Date().toString();
            String logMessage = date + " : Peer " + myPeerID + " is connected from Peer " + peerID + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void receiveHave(int peerID, int pieceIndex) {

        try {
            String date = new Date().toString();
            String logMessage = date + " : Peer " + myPeerID + " received the 'have' message from Peer " + peerID + " for the piece " + pieceIndex + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void logChoke(int peerID) {

        try {
            String date = new Date().toString();
            String logMessage = date + " This Peer is choked by " + peerID + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void logUnchoke(int peerID) {

        try {
            String date = new Date().toString();
            String logMessage = date + " This Peer is Unchoked by " + peerID + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void receiveInterested(int peerID) {

        try {
            String date = new Date().toString();
            String logMessage = date + " : Peer " + myPeerID + " received the 'interested' message from Peer " + peerID + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void receiveNotInterested(int peerID) {

        try {
            String date = new Date().toString();
            String logMessage = date + " : Peer " + myPeerID + " received the 'not interested' message from Peer " + peerID + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void downloadPiece(int peerID, int pieceIndex) {

        downloadedPieceCount++;
        try {
            String date = new Date().toString();
            String logMessage = date + " : Peer " + myPeerID + " has downloaded the piece " + pieceIndex +" from Peer " + peerID + ".";
            logWriter.append(logMessage);
            logWriter.newLine();
            logMessage = "Now  the number of pieces it has is " + downloadedPieceCount;
            logWriter.append(logMessage);
            logWriter.newLine();
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void downloadComplete() {

        if(fileFlag == true) {

            try {
                String date = new Date().toString();
                String logMessage = date + " : Peer " + myPeerID + " has downloaded the complete file.";
                logWriter.append(logMessage);
                logWriter.newLine();
                logWriter.newLine();
                logWriter.flush();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    public static void closeLogger() {
        try {
            logWriter.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}