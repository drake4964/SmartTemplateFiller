package com.example.smarttemplatefiller.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for checking file stability before processing.
 * Ensures a file is completely written before it's processed.
 */
public class FileStabilityChecker {
    private static final Logger logger = LoggerFactory.getLogger(FileStabilityChecker.class);

    private final int stabilityCheckSeconds;

    public FileStabilityChecker() {
        this(2); // Default 2 seconds
    }

    public FileStabilityChecker(int stabilityCheckSeconds) {
        if (stabilityCheckSeconds < 1 || stabilityCheckSeconds > 30) {
            throw new IllegalArgumentException("Stability check must be between 1 and 30 seconds");
        }
        this.stabilityCheckSeconds = stabilityCheckSeconds;
    }

    /**
     * Check if a file is stable (not being written to).
     * 
     * @param filePath Path to the file to check
     * @return true if file is stable and ready for processing
     */
    public boolean isStable(Path filePath) {
        if (!Files.exists(filePath)) {
            return false;
        }

        try {
            long initialSize = Files.size(filePath);
            logger.debug("Checking stability of {}, initial size: {} bytes", filePath, initialSize);

            TimeUnit.SECONDS.sleep(stabilityCheckSeconds);

            if (!Files.exists(filePath)) {
                logger.debug("File {} no longer exists after wait", filePath);
                return false;
            }

            long finalSize = Files.size(filePath);
            boolean stable = initialSize == finalSize;

            if (stable) {
                logger.debug("File {} is stable (size unchanged at {} bytes)", filePath, finalSize);
            } else {
                logger.debug("File {} is still being written (size changed from {} to {} bytes)",
                        filePath, initialSize, finalSize);
            }

            return stable;
        } catch (IOException e) {
            logger.error("Error checking file stability for {}: {}", filePath, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Stability check interrupted for {}", filePath);
            return false;
        }
    }

    /**
     * Wait for a file to become stable, with max retries.
     * 
     * @param filePath   Path to the file
     * @param maxRetries Maximum number of stability checks
     * @return true if file became stable within retry limit
     */
    public boolean waitForStability(Path filePath, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            if (isStable(filePath)) {
                return true;
            }
            logger.debug("File {} not stable, retry {} of {}", filePath, i + 1, maxRetries);
        }
        return false;
    }

    /**
     * Check if a file can be opened for reading (not locked).
     * 
     * @param filePath Path to the file
     * @return true if file can be read
     */
    public boolean isReadable(Path filePath) {
        try {
            Files.newInputStream(filePath).close();
            return true;
        } catch (IOException e) {
            logger.debug("File {} is not readable: {}", filePath, e.getMessage());
            return false;
        }
    }

    public int getStabilityCheckSeconds() {
        return stabilityCheckSeconds;
    }
}
