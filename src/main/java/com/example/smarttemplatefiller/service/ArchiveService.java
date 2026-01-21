package com.example.smarttemplatefiller.service;

import com.example.smarttemplatefiller.model.ArchiveConfiguration;
import com.example.smarttemplatefiller.model.TimestampFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service for archiving input files and output files together
 * in timestamped folders for traceability.
 */
public class ArchiveService {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveService.class);

    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private final ArchiveConfiguration config;

    public ArchiveService(ArchiveConfiguration config) {
        this.config = config;
    }

    /**
     * Create an archive folder with timestamp.
     * 
     * @return Path to the created archive folder
     */
    public Path createArchiveFolder() throws IOException {
        String timestamp = formatTimestamp(LocalDateTime.now());
        Path archiveFolder = config.getOutputFolder().resolve(timestamp);

        Files.createDirectories(archiveFolder);
        logger.info("Created archive folder: {}", archiveFolder);

        return archiveFolder;
    }

    /**
     * Create archive folder and inputs subfolder.
     * 
     * @return Path to the archive folder (parent of inputs/)
     */
    public Path createArchiveFolderWithInputs() throws IOException {
        Path archiveFolder = createArchiveFolder();
        Path inputsFolder = archiveFolder.resolve("inputs");
        Files.createDirectories(inputsFolder);
        return archiveFolder;
    }

    /**
     * Archive input files and output file together.
     * 
     * @param inputFiles Map of slot number to input file path
     * @param outputFile Path to the generated output file
     * @return Path to the archive folder
     */
    public Path archive(Map<Integer, Path> inputFiles, Path outputFile) throws IOException {
        Path archiveFolder = createArchiveFolderWithInputs();
        Path inputsFolder = archiveFolder.resolve("inputs");

        // Move output file to archive folder
        Path archivedOutput = archiveFolder.resolve(outputFile.getFileName());
        Files.move(outputFile, archivedOutput, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Archived output: {}", archivedOutput);

        // Move input files to inputs subfolder
        if (config.isArchiveInputFiles()) {
            for (Map.Entry<Integer, Path> entry : inputFiles.entrySet()) {
                Path inputFile = entry.getValue();
                if (Files.exists(inputFile)) {
                    Path archivedInput = inputsFolder.resolve(inputFile.getFileName());
                    Files.move(inputFile, archivedInput, StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("Archived input file slot {}: {}", entry.getKey(), archivedInput);
                }
            }
        }

        logger.info("Archive complete: {} input files + 1 output", inputFiles.size());
        return archiveFolder;
    }

    /**
     * Copy (not move) files to archive - useful when keeping originals.
     */
    public Path archiveCopy(Map<Integer, Path> inputFiles, Path outputFile) throws IOException {
        Path archiveFolder = createArchiveFolderWithInputs();
        Path inputsFolder = archiveFolder.resolve("inputs");

        // Copy output file
        Path archivedOutput = archiveFolder.resolve(outputFile.getFileName());
        Files.copy(outputFile, archivedOutput, StandardCopyOption.REPLACE_EXISTING);

        // Copy input files
        for (Map.Entry<Integer, Path> entry : inputFiles.entrySet()) {
            Path inputFile = entry.getValue();
            if (Files.exists(inputFile)) {
                Path archivedInput = inputsFolder.resolve(inputFile.getFileName());
                Files.copy(inputFile, archivedInput, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return archiveFolder;
    }

    /**
     * Format timestamp based on configuration.
     */
    private String formatTimestamp(LocalDateTime dateTime) {
        if (config.getTimestampFormat() == TimestampFormat.DATE_ONLY) {
            return dateTime.format(DATE_ONLY_FORMAT);
        }
        return dateTime.format(DATETIME_FORMAT);
    }

    /**
     * Get the configured output folder.
     */
    public Path getOutputFolder() {
        return config.getOutputFolder();
    }
}
