package com.example.smarttemplatefiller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Background service that watches a folder for incoming files
 * and processes them using the configured mapping.
 */
public class FolderWatcher {

    private Path watchFolder;
    private Set<String> fileExtensions;
    private File mappingFile;
    private Path outputFolder;
    private int intervalSeconds;
    private Consumer<String> logCallback;

    // Append mode configuration (T018)
    private boolean appendModeEnabled;
    private String lastGeneratedFilePath;

    private ScheduledExecutorService scheduler;
    private volatile boolean running;

    // Track files currently being processed to avoid duplicate processing
    private final Set<String> processingFiles = new HashSet<>();

    public FolderWatcher(RunningModeConfig config, Consumer<String> logCallback) {
        this.watchFolder = Paths.get(config.getWatchFolder());
        this.mappingFile = new File(config.getMappingFile());
        this.outputFolder = Paths.get(config.getOutputFolder());
        this.intervalSeconds = config.getIntervalSeconds();
        this.logCallback = logCallback;

        // T018: Read append mode settings from config
        this.appendModeEnabled = config.isAppendModeEnabled();
        this.lastGeneratedFilePath = config.getLastGeneratedFilePath();

        // Parse file extensions from pattern (e.g., "*.txt,*.asc" -> ["txt", "asc"])
        this.fileExtensions = new HashSet<>();
        for (String pattern : config.getFilePattern().split(",")) {
            pattern = pattern.trim();
            if (pattern.startsWith("*.")) {
                fileExtensions.add(pattern.substring(2).toLowerCase());
            }
        }
    }

    /**
     * Start the folder watcher.
     */
    public void start() {
        if (running) {
            log("Watcher already running");
            return;
        }

        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::scanFolder, 0, intervalSeconds, TimeUnit.SECONDS);
        log("Started watching: " + watchFolder);
        log("Pattern: " + String.join(", ", fileExtensions.stream().map(e -> "*." + e).toArray(String[]::new)));
        if (appendModeEnabled) {
            log("Append Mode: ENABLED" + (lastGeneratedFilePath != null
                    ? " (continuing to: " + new File(lastGeneratedFilePath).getName() + ")"
                    : ""));
        }
    }

    /**
     * Stop the folder watcher.
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        log("Stopped watching");
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Get the last generated file path for session persistence.
     */
    public String getLastGeneratedFilePath() {
        return lastGeneratedFilePath;
    }

    /**
     * Scan folder for matching files and process them.
     */
    private void scanFolder() {
        if (!running)
            return;

        try {
            File folder = watchFolder.toFile();
            if (!folder.exists() || !folder.isDirectory()) {
                return;
            }

            File[] files = folder.listFiles();
            if (files == null)
                return;

            for (File file : files) {
                if (!running)
                    break;
                if (!file.isFile())
                    continue;

                String fileName = file.getName().toLowerCase();
                String extension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    extension = fileName.substring(dotIndex + 1);
                }

                if (fileExtensions.contains(extension)) {
                    processFile(file);
                }
            }
        } catch (Exception e) {
            log("Error scanning folder: " + e.getMessage());
        }
    }

    /**
     * Process a single file: convert to Excel and archive.
     * T019/T020: Handles append mode with file deleted detection.
     */
    private void processFile(File sourceFile) {
        String fileName = sourceFile.getName();

        // Skip if already being processed
        synchronized (processingFiles) {
            if (processingFiles.contains(fileName)) {
                return;
            }
            processingFiles.add(fileName);
        }

        try {
            log("Processing: " + fileName);

            // Create timestamp folder structure for archive
            String mappingName = mappingFile.getName().replace(".json", "");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
            Path timestampFolder = outputFolder.resolve(mappingName).resolve(timestamp);
            Path archiveFolder = timestampFolder.resolve("archive");

            // T019/T020: Check if we should append to existing file
            if (appendModeEnabled && lastGeneratedFilePath != null) {
                File existingFile = new File(lastGeneratedFilePath);

                // T020: Check if file was deleted
                if (existingFile.exists()) {
                    // Append to existing file
                    AppendResult result = ExcelWriter.appendToMappedFile(sourceFile, mappingFile, existingFile);

                    if (result.isSuccess()) {
                        log("Appended " + result.getRowsAdded() + " rows to " + existingFile.getName() +
                                " (offset: " + result.getRowOffset() + ")");

                        // Log any warnings
                        for (String warning : result.getWarnings()) {
                            log("WARNING: " + warning);
                        }

                        // Archive source file to the original output folder's archive
                        Path originalFolder = existingFile.toPath().getParent();
                        Path originalArchive = originalFolder.resolve("archive");
                        Files.createDirectories(originalArchive);
                        Path archivePath = originalArchive.resolve(fileName);
                        Files.move(sourceFile.toPath(), archivePath, StandardCopyOption.REPLACE_EXISTING);
                        log("Archived: " + fileName);
                        return;
                    } else {
                        log("ERROR: Failed to append: " + result.getErrorMessage());
                        log("Creating new file instead...");
                        // Fall through to create new file
                    }
                } else {
                    // T020: File was deleted, warn and create new
                    log("WARNING: Target file was deleted: " + existingFile.getName());
                    log("Creating new file instead...");
                }
            }

            // Create new file (either first file or append not enabled/failed)
            Files.createDirectories(archiveFolder);

            // Generate output file name
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            File outputFile = timestampFolder.resolve(baseName + ".xlsx").toFile();

            // Convert using ExcelWriter
            ExcelWriter.writeAdvancedMappedFile(sourceFile, mappingFile, outputFile);
            log("Created new file: " + outputFile.getName());

            // T019: Store path for subsequent appends
            if (appendModeEnabled) {
                lastGeneratedFilePath = outputFile.getAbsolutePath();
                log("Append target set to: " + outputFile.getName());
            }

            // Move source file to archive
            Path archivePath = archiveFolder.resolve(fileName);
            Files.move(sourceFile.toPath(), archivePath, StandardCopyOption.REPLACE_EXISTING);
            log("Archived: " + fileName);

        } catch (Exception e) {
            log("Error processing " + fileName + ": " + e.getMessage());
        } finally {
            synchronized (processingFiles) {
                processingFiles.remove(fileName);
            }
        }
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = timestamp + " - " + message;
        if (logCallback != null) {
            logCallback.accept(logMessage);
        }
        System.out.println("[FolderWatcher] " + logMessage);
    }
}
