
package fileparsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import nodeoperations.Piece;

public class FileChunkReader {

    private static final String PEER_DIRECTORY_PREFIX = "peer_";

    private final int peerId;
    private final long chunkSize;
    private final String targetFileName;
    private final Map<Integer, Piece> fileChunks;
    private int currentChunkIndex;

    /**
     * Creates a new FileChunkReader instance
     *
     * @param peerId The ID of the peer
     * @param chunkSize Size of each file chunk in bytes
     * @param targetFileName Name of the file to read
     */
    public FileChunkReader(int peerId, long chunkSize, String targetFileName) {
        this.peerId = peerId;
        this.chunkSize = chunkSize;
        this.targetFileName = targetFileName;
        this.fileChunks = new HashMap<>();
        this.currentChunkIndex = 1;
    }

    /**
     * Reads the file and splits it into chunks
     *
     * @return Map of chunk index to Piece objects
     * @throws FileProcessingException if there's an error reading the file
     */
    public Map<Integer, Piece> processFile() {
        Path filePath = constructFilePath();
        File sourceFile = filePath.toFile();

        validateFile(sourceFile);

        try (FileInputStream fileStream = new FileInputStream(sourceFile)) {
            readFileChunks(fileStream);
        } catch (IOException e) {
            throw new FileProcessingException("Error processing file: " + filePath, e);
        }

        return fileChunks;
    }

    /**
     * Constructs the complete file path based on peer ID and filename
     *
     * @return Path object representing the file location
     */
    private Path constructFilePath() {
       // String baseDirectory = new File(System.getProperty("user.dir")).getParent();
        String baseDirectory = System.getProperty("user.dir");
        String peerDirectory = PEER_DIRECTORY_PREFIX + peerId;
        return Paths.get(baseDirectory, peerDirectory, targetFileName);
    }

    /**
     * Validates that the file exists and is readable
     *
     * @param file File to validate
     * @throws FileProcessingException if file is invalid
     */
    private void validateFile(File file) {
        if (!file.exists()) {
            throw new FileProcessingException("File does not exist: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new FileProcessingException("Cannot read file: " + file.getPath());
        }
    }

    /**
     * Reads the file contents and splits into chunks
     *
     * @param fileStream Input stream for the file
     * @throws IOException if there's an error reading the file
     */
    private void readFileChunks(FileInputStream fileStream) throws IOException {
        byte[] buffer = new byte[(int)chunkSize];
        int bytesRead;

        while ((bytesRead = fileStream.read(buffer)) > 0) {
            byte[] chunkData = bytesRead == buffer.length ? buffer : trimBuffer(buffer, bytesRead);

            fileChunks.put(currentChunkIndex, new Piece(currentChunkIndex, chunkData));
            currentChunkIndex++;
        }
    }

    /**
     * Creates a new buffer with exact size for the last chunk
     *
     * @param buffer Original buffer
     * @param size Actual number of bytes read
     * @return Properly sized buffer
     */
    private byte[] trimBuffer(byte[] buffer, int size) {
        byte[] trimmedBuffer = new byte[size];
        System.arraycopy(buffer, 0, trimmedBuffer, 0, size);
        return trimmedBuffer;
    }

    /**
     * Custom exception for file processing errors
     */
    public static class FileProcessingException extends RuntimeException {
        public FileProcessingException(String message) {
            super(message);
        }

        public FileProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}