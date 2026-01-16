package com.example.smarttemplatefiller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration POJO for Running Mode settings.
 * Persists user configuration between sessions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunningModeConfig {

    private String mappingFile;
    private String watchFolder;
    private String outputFolder;
    private String filePattern = "*.txt,*.asc";
    private int intervalSeconds = 1;

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.smarttemplatefiller";
    private static final String CONFIG_FILE = CONFIG_DIR + "/running_mode_config.json";

    // Default constructor for Jackson
    public RunningModeConfig() {
    }

    // Getters and Setters
    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    public String getWatchFolder() {
        return watchFolder;
    }

    public void setWatchFolder(String watchFolder) {
        this.watchFolder = watchFolder;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    /**
     * Save configuration to user's home directory.
     */
    public void save() throws IOException {
        Path configDir = Paths.get(CONFIG_DIR);
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_FILE), this);
    }

    /**
     * Load configuration from user's home directory.
     * Returns default config if file doesn't exist.
     */
    public static RunningModeConfig load() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(configFile, RunningModeConfig.class);
            } catch (IOException e) {
                System.err.println("Failed to load config, using defaults: " + e.getMessage());
            }
        }
        return new RunningModeConfig();
    }
}
