package com.example.smarttemplatefiller.service;

import com.example.smarttemplatefiller.model.MatchingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for matching files across watched folders.
 * Supports prefix-based matching (text before first underscore) and exact
 * basename matching.
 */
public class FileMatchingService {
    private static final Logger logger = LoggerFactory.getLogger(FileMatchingService.class);

    private final MatchingStrategy strategy;

    public FileMatchingService() {
        this(MatchingStrategy.PREFIX);
    }

    public FileMatchingService(MatchingStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Extract the matching key from a filename based on the configured strategy.
     * 
     * @param filePath Path to the file
     * @return The matching key (prefix or basename)
     */
    public String extractMatchKey(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return extractMatchKey(fileName);
    }

    /**
     * Extract the matching key from a filename string.
     * 
     * @param fileName File name (with or without extension)
     * @return The matching key
     */
    public String extractMatchKey(String fileName) {
        // Remove extension
        String baseName = removeExtension(fileName);

        if (strategy == MatchingStrategy.PREFIX) {
            // Extract text before first underscore
            int underscoreIndex = baseName.indexOf('_');
            if (underscoreIndex > 0) {
                return baseName.substring(0, underscoreIndex);
            }
        }

        // For EXACT_BASENAME or no underscore found, return full basename
        return baseName;
    }

    /**
     * Remove file extension from filename.
     */
    private String removeExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }

    /**
     * Check if two files match based on the configured strategy.
     * 
     * @param file1 First file path
     * @param file2 Second file path
     * @return true if files match
     */
    public boolean filesMatch(Path file1, Path file2) {
        String key1 = extractMatchKey(file1);
        String key2 = extractMatchKey(file2);
        boolean match = key1.equals(key2);
        logger.debug("Comparing {} ({}) with {} ({}): match={}",
                file1.getFileName(), key1, file2.getFileName(), key2, match);
        return match;
    }

    /**
     * Group files by their match key.
     * 
     * @param files Collection of file paths
     * @return Map of match key to list of files with that key
     */
    public Map<String, List<Path>> groupByMatchKey(Collection<Path> files) {
        return files.stream()
                .collect(Collectors.groupingBy(this::extractMatchKey));
    }

    /**
     * Find files from a map of slot -> file path that all share the same match key.
     * Used to determine if all required files for processing are present.
     * 
     * @param slotFiles Map of slot number to file path
     * @return Optional containing the common match key if all files match, empty
     *         otherwise
     */
    public Optional<String> findCommonMatchKey(Map<Integer, Path> slotFiles) {
        if (slotFiles.isEmpty()) {
            return Optional.empty();
        }

        Set<String> keys = slotFiles.values().stream()
                .map(this::extractMatchKey)
                .collect(Collectors.toSet());

        if (keys.size() == 1) {
            return Optional.of(keys.iterator().next());
        }

        logger.debug("No common match key found. Keys: {}", keys);
        return Optional.empty();
    }

    public MatchingStrategy getStrategy() {
        return strategy;
    }
}
