package com.example.smarttemplatefiller;

/**
 * Configuration POJO for export dialog settings.
 * Holds append mode preference and target path.
 */
public class ExportConfiguration {

    private boolean appendMode;
    private String appendTargetPath;

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
}
