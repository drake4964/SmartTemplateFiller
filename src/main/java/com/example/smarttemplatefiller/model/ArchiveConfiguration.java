package com.example.smarttemplatefiller.model;

import java.nio.file.Path;

/**
 * Configuration for post-processing archiving.
 */
public class ArchiveConfiguration {
    private Path outputFolder;
    private TimestampFormat timestampFormat;
    private boolean archiveInputFiles;

    public ArchiveConfiguration() {
        this.timestampFormat = TimestampFormat.DATETIME;
        this.archiveInputFiles = true;
    }

    public ArchiveConfiguration(Path outputFolder, TimestampFormat timestampFormat) {
        this.outputFolder = outputFolder;
        this.timestampFormat = timestampFormat;
        this.archiveInputFiles = true;
    }

    // Getters and Setters
    public Path getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    public TimestampFormat getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(TimestampFormat timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public boolean isArchiveInputFiles() {
        return archiveInputFiles;
    }

    public void setArchiveInputFiles(boolean archiveInputFiles) {
        this.archiveInputFiles = archiveInputFiles;
    }

    @Override
    public String toString() {
        return "ArchiveConfiguration{" +
                "outputFolder=" + outputFolder +
                ", timestampFormat=" + timestampFormat +
                ", archiveInputFiles=" + archiveInputFiles +
                '}';
    }
}
