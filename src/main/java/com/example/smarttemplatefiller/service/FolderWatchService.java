package com.example.smarttemplatefiller.service;

import com.example.smarttemplatefiller.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service for watching multiple folders and triggering auto-processing
 * when all required input files are present.
 */
public class FolderWatchService {
    private static final Logger logger = LoggerFactory.getLogger(FolderWatchService.class);

    private final FileMatchingService fileMatchingService;
    private final com.example.smarttemplatefiller.util.FileStabilityChecker stabilityChecker;
    private final Map<Integer, WatchFolder> watchFolders = new ConcurrentHashMap<>();
    private final Map<Integer, Path> detectedFiles = new ConcurrentHashMap<>();
    private final List<FolderWatchListener> listeners = new CopyOnWriteArrayList<>();

    private WatchService watchService;
    private ExecutorService watchExecutor;
    private volatile boolean isWatching = false;

    public FolderWatchService(WatchConfiguration config) {
        this.fileMatchingService = new FileMatchingService(config.getMatchingStrategy());
        this.stabilityChecker = new com.example.smarttemplatefiller.util.FileStabilityChecker(
                config.getStabilityCheckSeconds());
    }

    /**
     * Add a folder to watch for a specific file slot.
     */
    public void addWatchFolder(int slot, Path folderPath) throws IllegalArgumentException {
        if (slot < 1 || slot > 10) {
            throw new IllegalArgumentException("Slot must be between 1 and 10");
        }
        if (!Files.isDirectory(folderPath)) {
            throw new IllegalArgumentException("Path is not a directory: " + folderPath);
        }

        WatchFolder watchFolder = new WatchFolder(folderPath, slot);
        watchFolders.put(slot, watchFolder);
        logger.info("Added watch folder for slot {}: {}", slot, folderPath);
    }

    /**
     * Start watching all configured folders.
     */
    public void startWatching() throws IOException {
        if (isWatching) {
            logger.warn("Already watching folders");
            return;
        }

        if (watchFolders.isEmpty()) {
            throw new IllegalStateException("No folders configured for watching");
        }

        watchService = FileSystems.getDefault().newWatchService();

        for (WatchFolder folder : watchFolders.values()) {
            folder.getFolderPath().register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            folder.setActive(true);
        }

        watchExecutor = Executors.newSingleThreadExecutor();
        isWatching = true;

        watchExecutor.submit(this::watchLoop);
        logger.info("Started watching {} folders", watchFolders.size());
    }

    /**
     * Stop watching all folders.
     */
    public void stopWatching() {
        isWatching = false;

        if (watchExecutor != null) {
            watchExecutor.shutdownNow();
        }

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.error("Error closing watch service", e);
            }
        }

        watchFolders.values().forEach(f -> f.setActive(false));
        detectedFiles.clear();
        logger.info("Stopped watching folders");
    }

    /**
     * Main watch loop - runs in separate thread.
     */
    private void watchLoop() {
        logger.debug("Watch loop started");

        while (isWatching) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    continue;
                }

                Path watchedDir = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path fileName = (Path) event.context();
                    Path fullPath = watchedDir.resolve(fileName);

                    handleFileEvent(watchedDir, fullPath);
                }

                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }
        }

        logger.debug("Watch loop ended");
    }

    /**
     * Handle a file creation/modification event.
     */
    private void handleFileEvent(Path watchedDir, Path filePath) {
        if (!Files.isRegularFile(filePath)) {
            return;
        }

        // Find which slot this folder belongs to
        Integer slot = findSlotForFolder(watchedDir);
        if (slot == null) {
            logger.warn("Received event for unknown folder: {}", watchedDir);
            return;
        }

        logger.debug("File event in slot {}: {}", slot, filePath.getFileName());

        // Check file stability in background
        CompletableFuture.runAsync(() -> {
            if (stabilityChecker.isStable(filePath)) {
                onFileReady(slot, filePath);
            }
        });
    }

    /**
     * Find the slot number for a watched folder path.
     */
    private Integer findSlotForFolder(Path folderPath) {
        for (Map.Entry<Integer, WatchFolder> entry : watchFolders.entrySet()) {
            if (entry.getValue().getFolderPath().equals(folderPath)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Called when a file is confirmed ready in a slot.
     */
    private void onFileReady(int slot, Path filePath) {
        logger.info("File ready in slot {}: {}", slot, filePath.getFileName());

        WatchFolder folder = watchFolders.get(slot);
        if (folder != null) {
            folder.setLastFileDetected(filePath);
        }

        detectedFiles.put(slot, filePath);
        notifyFileReady(slot, filePath);

        // Check if all required files are present
        checkAllFilesReady();
    }

    /**
     * Check if all required files are present and matching.
     */
    private void checkAllFilesReady() {
        if (detectedFiles.size() < watchFolders.size()) {
            logger.debug("Waiting for more files: {}/{}",
                    detectedFiles.size(), watchFolders.size());
            return;
        }

        // Check if all files have matching prefix
        Optional<String> commonKey = fileMatchingService.findCommonMatchKey(detectedFiles);

        if (commonKey.isPresent()) {
            logger.info("All files ready with common key: {}", commonKey.get());
            notifyAllFilesReady(commonKey.get(), new HashMap<>(detectedFiles));
            detectedFiles.clear(); // Reset for next batch
        } else {
            logger.debug("Files don't match, waiting for matching set");
        }
    }

    // Listener management
    public void addListener(FolderWatchListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FolderWatchListener listener) {
        listeners.remove(listener);
    }

    private void notifyFileReady(int slot, Path filePath) {
        for (FolderWatchListener listener : listeners) {
            try {
                listener.onFileReady(slot, filePath);
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
    }

    private void notifyAllFilesReady(String matchKey, Map<Integer, Path> files) {
        for (FolderWatchListener listener : listeners) {
            try {
                listener.onAllFilesReady(matchKey, files);
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
    }

    // Status methods
    public boolean isWatching() {
        return isWatching;
    }

    public Map<Integer, WatchFolder> getWatchFolders() {
        return Collections.unmodifiableMap(watchFolders);
    }

    public Map<Integer, Path> getDetectedFiles() {
        return Collections.unmodifiableMap(detectedFiles);
    }

    /**
     * Listener interface for folder watch events.
     */
    public interface FolderWatchListener {
        void onFileReady(int slot, Path filePath);

        void onAllFilesReady(String matchKey, Map<Integer, Path> files);
    }
}
