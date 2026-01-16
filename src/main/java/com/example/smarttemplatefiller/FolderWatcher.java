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
            // Create timestamp folder structure
            String mappingName = mappingFile.getName().replace(".json", "");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
            Path timestampFolder = outputFolder.resolve(mappingName).resolve(timestamp);
            Path archiveFolder = timestampFolder.resolve("archive");

            Files.createDirectories(archiveFolder);

            // Generate output file name
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            File outputFile = timestampFolder.resolve(baseName + ".xlsx").toFile();

            // Convert using ExcelWriter
            log("Processing: " + fileName);
            ExcelWriter.writeAdvancedMappedFile(sourceFile, mappingFile, outputFile);
            log("Created: " + outputFile.getName());

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
