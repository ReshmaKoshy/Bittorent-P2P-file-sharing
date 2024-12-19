package fileparsers;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PeerConfigurationReader {
    private static final String CONFIG_FILE_NAME = "Common.cfg";
    
    private static final String CONFIG_PATH = System.getProperty("user.dir") + File.separator + CONFIG_FILE_NAME;
    // Configuration properties
    private int activeNeighborCount;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String targetFileName;
    private long totalFileSize;
    private long chunkSize;

    // Configuration keys
    private static final String NEIGHBOR_COUNT_KEY = "NumberOfPreferredNeighbors";
    private static final String UNBLOCK_INTERVAL_KEY = "UnchokingInterval";
    private static final String RANDOM_UNBLOCK_INTERVAL_KEY = "OptimisticUnchokingInterval";
    private static final String FILE_NAME_KEY = "FileName";
    private static final String FILE_SIZE_KEY = "FileSize";
    private static final String CHUNK_SIZE_KEY = "PieceSize";

    /**
     * Loads and parses the configuration file
     * @throws ConfigurationException if there's an error reading the configuration
     */
    public void loadConfiguration() {
        Properties configProperties = new Properties();

        try (BufferedInputStream configStream = new BufferedInputStream(
                new FileInputStream(CONFIG_PATH))) {
            System.out.println("file path of config file"+CONFIG_PATH);            
            configProperties.load(configStream);
            parseConfigurationValues(configProperties);

        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Configuration file not found: "+CONFIG_PATH , e);
        } catch (IOException e) {
            throw new ConfigurationException("Error reading configuration file", e);
        }
    }

    /**
     * Parses individual configuration values from properties
     * @param properties The loaded configuration properties
     */
    private void parseConfigurationValues(Properties properties) {
        try {
            activeNeighborCount = Integer.parseInt(properties.getProperty(NEIGHBOR_COUNT_KEY));
            unchokingInterval = Integer.parseInt(properties.getProperty(UNBLOCK_INTERVAL_KEY));
            optimisticUnchokingInterval = Integer.parseInt(properties.getProperty(RANDOM_UNBLOCK_INTERVAL_KEY));
            targetFileName = properties.getProperty(FILE_NAME_KEY);
            totalFileSize = Long.parseLong(properties.getProperty(FILE_SIZE_KEY));
            chunkSize = Long.parseLong(properties.getProperty(CHUNK_SIZE_KEY));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid numeric value in configuration", e);
        }
    }

    // Getters with improved documentation
    /**
     * @return The number of active neighbors to maintain
     */
    public int getActiveNeighborCount() {
        return activeNeighborCount;
    }

    /**
     * @return The interval (in seconds) for regular peer unblocking
     */
    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    /**
     * @return The interval (in seconds) for random peer unblocking
     */
    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    /**
     * @return The name of the target file to be transferred
     */
    public String getTargetFileName() {
        return targetFileName;
    }

    /**
     * @return The total size of the file in bytes
     */
    public long getTotalFileSize() {
        return totalFileSize;
    }

    /**
     * @return The size of each file chunk in bytes
     */
    public long getChunkSize() {
        return chunkSize;
    }

    /**
     * Custom exception for configuration-related errors
     */
    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}