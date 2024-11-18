package fileparsers;

import java.io.IOException;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.nio.file.NoSuchFileException;
import java.io.FileNotFoundException;

public class PeerInfoFileParse { 

    private final Path filepath = Paths.get(System.getProperty("user.dir")).resolve("PeerInfo.cfg");

    private int peerID;
    private String peerHost; 
    private int portno; 
    private final int self_ID; 
    private boolean hasFullFile; 

    //constructor to set original Peer Id
    public PeerInfoFileParse(int self_ID){
        this.self_ID = self_ID;
    }

    // Getters
    public int getOwnPeerId() { 
        return self_ID;
    }

    public String getPeerHost() { 
        return peerHost;
    }

    public int getPort() { 
        return portno;
    }

    public boolean isFullFile() {   
        return hasFullFile;
    }

    //Setting all the values for the original Peer
    public void setPeerValues() {  
        try (BufferedReader reader = Files.newBufferedReader(filepath)) {
            String row; 
            System.out.println("file path of peer info file"+filepath);
            for (int lineNum = 1; (row = reader.readLine()) != null; lineNum++) {
                try {
                    String[] eachRow = row.split(" ");
                    if (self_ID == Integer.parseInt(eachRow[0])) {
                        peerID = Integer.parseInt(eachRow[0]);
                        peerHost = eachRow[1];
                        portno = Integer.parseInt(eachRow[2]);
                        hasFullFile = eachRow[3].equals("1");
                        break;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error parsing row " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            System.err.println("Error: PeerInfo.cfg file not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
    }

    //Get details of all Peers prior to Original Peer, so that it can connect with them
    public ArrayList<String[]> getPrecedingPeerDetails() { 
        ArrayList<String[]> arr = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filepath)) {
            String row; 
            for (int lineNum = 1; (row = reader.readLine()) != null; lineNum++) {
                try {
                    String[] eachRow = row.split(" ");
                    if (self_ID != Integer.parseInt(eachRow[0])) {
                        arr.add(eachRow);
                    } else {
                        break;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error parsing line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            System.err.println("Error: PeerInfo.cfg file not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
        return arr;
    }

    //Getting all the Peer IDs
    public ArrayList<Integer> getFullPeerIds() { 
        ArrayList<Integer> arr = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filepath)) {
            String row; 
            for (int lineNum = 1; (row = reader.readLine()) != null; lineNum++) {
                try {
                    String[] eachRow = row.split(" ");
                    arr.add(Integer.parseInt(eachRow[0]));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error parsing line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            System.err.println("Error: PeerInfo.cfg file not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
        return arr;
    }

}
