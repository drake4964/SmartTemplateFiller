package com.example.smarttemplatefiller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration POJO for export dialog settings.
 * Holds append mode preference and target path.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportConfiguration {

    private boolean appendMode;
    private String appendTargetPath;

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.smarttemplatefiller";
    private static final String CONFIG_FILE = CONFIG_DIR + "/export_configuration.json";

    /**
     * Default constructor with append mode disabled.
     */
    public ExportConfiguration() {
        this.appendMode = false;
        this.appendTargetPath = null;
    }

    /**
     * Constructor with append mode settings.
     */
    public ExportConfiguration(boolean appendMode, String appendTargetPath) {
        this.appendMode = appendMode;
        this.appendTargetPath = appendTargetPath;
    }

    // Getters and Setters

    public boolean isAppendMode() {
        return appendMode;
    }

    public void setAppendMode(boolean appendMode) {
        this.appendMode = appendMode;
    }

    public String getAppendTargetPath() {
        return appendTargetPath;
    }

    public void setAppendTargetPath(String appendTargetPath) {
        this.appendTargetPath = appendTargetPath;
    }

    @Override
    public String toString() {
        return String.format("ExportConfiguration[appendMode=%s, targetPath=%s]",
                appendMode, appendTargetPath);
    }

    /**
     * Save configuration to user's home directory.
     */
    public void save() {
        try {
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_FILE), this);
        } catch (IOException e) {
            System.err.println("Failed to save export config: " + e.getMessage());
        }
    }

    /**
     * Load configuration from user's home directory.
     * Returns default config if file doesn't exist.
     */
    public static ExportConfiguration load() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(configFile, ExportConfiguration.class);
            } catch (IOException e) {
                System.err.println("Failed to load export config, using defaults: " + e.getMessage());
            }
        }
        return new ExportConfiguration();
    }
}
