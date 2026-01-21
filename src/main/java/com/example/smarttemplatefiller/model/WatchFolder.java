package com.example.smarttemplatefiller.model;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a folder to monitor for automatic processing.
 * Linked to a source file slot (1-10).
 */
public class WatchFolder {
    private String id;
    private Path folderPath;
    private int linkedSlot;
    private boolean isActive;
    private Path lastFileDetected;

    public WatchFolder() {
        this.id = UUID.randomUUID().toString();
        this.isActive = false;
    }

    public WatchFolder(Path folderPath, int linkedSlot) {
        this();
        this.folderPath = folderPath;
        this.linkedSlot = linkedSlot;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Path getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(Path folderPath) {
        this.folderPath = folderPath;
    }

    public int getLinkedSlot() {
        return linkedSlot;
    }

    public void setLinkedSlot(int linkedSlot) {
        if (linkedSlot < 1 || linkedSlot > 10) {
            throw new IllegalArgumentException("Linked slot must be between 1 and 10");
        }
        this.linkedSlot = linkedSlot;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Path getLastFileDetected() {
        return lastFileDetected;
    }

    public void setLastFileDetected(Path lastFileDetected) {
        this.lastFileDetected = lastFileDetected;
    }

    /**
     * Check if this watch folder has a file ready for processing.
     */
    public boolean hasFileReady() {
        return lastFileDetected != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WatchFolder that = (WatchFolder) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WatchFolder{" +
                "folderPath=" + folderPath +
                ", linkedSlot=" + linkedSlot +
                ", isActive=" + isActive +
                '}';
    }
}
