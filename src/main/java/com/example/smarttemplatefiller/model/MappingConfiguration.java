package com.example.smarttemplatefiller.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Extended configuration container with schema versioning.
 * Supports multi-file mappings and folder watching.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingConfiguration {
    public static final String CURRENT_SCHEMA_VERSION = "2.0";

    private String schemaVersion;
    private List<MultiFileMapping> mappings;
    private List<FileSlot> fileSlots;
    private WatchConfiguration watchConfig;
    private ArchiveConfiguration archiveConfig;

    public MappingConfiguration() {
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
        this.mappings = new ArrayList<>();
        this.fileSlots = new ArrayList<>();
    }

    /**
     * Create a single-file configuration (backward compatible with v1.0).
     */
    public static MappingConfiguration createSingleFile() {
        MappingConfiguration config = new MappingConfiguration();
        config.getFileSlots().add(new FileSlot(1, "Default"));
        return config;
    }

    /**
     * Check if this is a multi-file configuration.
     */
    public boolean isMultiFile() {
        return fileSlots.size() > 1;
    }

    /**
     * Get the number of required files for this configuration.
     */
    public int getRequiredFileCount() {
        return fileSlots.size();
    }

    // Getters and Setters
    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<MultiFileMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<MultiFileMapping> mappings) {
        this.mappings = mappings;
    }

    public void addMapping(MultiFileMapping mapping) {
        this.mappings.add(mapping);
    }

    public List<FileSlot> getFileSlots() {
        return fileSlots;
    }

    public void setFileSlots(List<FileSlot> fileSlots) {
        this.fileSlots = fileSlots;
    }

    public void addFileSlot(FileSlot slot) {
        if (this.fileSlots.size() >= 10) {
            throw new IllegalStateException("Maximum 10 file slots allowed");
        }
        this.fileSlots.add(slot);
    }

    public WatchConfiguration getWatchConfig() {
        return watchConfig;
    }

    public void setWatchConfig(WatchConfiguration watchConfig) {
        this.watchConfig = watchConfig;
    }

    public ArchiveConfiguration getArchiveConfig() {
        return archiveConfig;
    }

    public void setArchiveConfig(ArchiveConfiguration archiveConfig) {
        this.archiveConfig = archiveConfig;
    }

    @Override
    public String toString() {
        return "MappingConfiguration{" +
                "schemaVersion='" + schemaVersion + '\'' +
                ", mappings=" + mappings.size() +
                ", fileSlots=" + fileSlots.size() +
                '}';
    }
}
