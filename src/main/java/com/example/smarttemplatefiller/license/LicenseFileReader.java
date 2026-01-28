package com.example.smarttemplatefiller.license;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads and parses license JSON files.
 * Handles file I/O and JSON deserialization with proper error handling.
 */
public class LicenseFileReader {

    private static final Logger LOGGER = Logger.getLogger(LicenseFileReader.class.getName());

    private final ObjectMapper objectMapper;

    /**
     * Creates a new LicenseFileReader with default ObjectMapper.
     */
    public LicenseFileReader() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates a new LicenseFileReader with custom ObjectMapper.
     *
     * @param objectMapper Custom Jackson ObjectMapper
     */
    public LicenseFileReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Reads and parses a license file from the specified path.
     *
     * @param filePath Path to the license JSON file
     * @return Parsed LicenseData object
     * @throws LicenseReadException if file not found or parsing fails
     */
    public LicenseData read(String filePath) throws LicenseReadException {
        if (filePath == null || filePath.isEmpty()) {
            throw new LicenseReadException("License file path is null or empty",
                    LicenseReadException.ErrorType.INVALID_PATH);
        }

        File file = new File(filePath);

        // Security: Prevent directory traversal
        try {
            String canonicalPath = file.getCanonicalPath();
            if (canonicalPath.contains("..")) {
                throw new LicenseReadException("Invalid file path (directory traversal detected)",
                        LicenseReadException.ErrorType.INVALID_PATH);
            }
        } catch (IOException e) {
            throw new LicenseReadException("Failed to resolve file path",
                    LicenseReadException.ErrorType.INVALID_PATH, e);
        }

        if (!file.exists()) {
            LOGGER.log(Level.INFO, "License file not found: {0}", filePath);
            throw new LicenseReadException("License file not found: " + filePath,
                    LicenseReadException.ErrorType.FILE_NOT_FOUND);
        }

        if (!file.isFile()) {
            throw new LicenseReadException("Path is not a file: " + filePath,
                    LicenseReadException.ErrorType.INVALID_PATH);
        }

        if (!file.canRead()) {
            throw new LicenseReadException("Cannot read license file: " + filePath,
                    LicenseReadException.ErrorType.FILE_NOT_FOUND);
        }

        try {
            LicenseData data = objectMapper.readValue(file, LicenseData.class);

            // Validate required fields
            if (!data.isComplete()) {
                throw new LicenseReadException("License file is missing required fields",
                        LicenseReadException.ErrorType.INVALID_FORMAT);
            }

            LOGGER.log(Level.FINE, "Successfully read license file: {0}", filePath);
            return data;

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to parse license file: " + filePath, e);
            throw new LicenseReadException("Failed to parse license file: " + e.getMessage(),
                    LicenseReadException.ErrorType.INVALID_FORMAT, e);
        }
    }

    /**
     * Exception thrown when license file reading fails.
     */
    public static class LicenseReadException extends Exception {

        public enum ErrorType {
            FILE_NOT_FOUND,
            INVALID_PATH,
            INVALID_FORMAT
        }

        private final ErrorType errorType;

        public LicenseReadException(String message, ErrorType errorType) {
            super(message);
            this.errorType = errorType;
        }

        public LicenseReadException(String message, ErrorType errorType, Throwable cause) {
            super(message, cause);
            this.errorType = errorType;
        }

        public ErrorType getErrorType() {
            return errorType;
        }
    }
}
