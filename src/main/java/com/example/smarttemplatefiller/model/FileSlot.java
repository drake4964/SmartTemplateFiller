package com.example.smarttemplatefiller.model;

import java.util.Objects;

/**
 * Represents a file input slot in a multi-file configuration.
 * Each slot corresponds to one input file position (1-10).
 */
public class FileSlot {
    private int slot;
    private String description;
    private String expectedPattern;

    public FileSlot() {
    }

    public FileSlot(int slot, String description) {
        this.slot = slot;
        this.description = description;
    }

    // Getters and Setters
    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        if (slot < 1 || slot > 10) {
            throw new IllegalArgumentException("Slot must be between 1 and 10");
        }
        this.slot = slot;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpectedPattern() {
        return expectedPattern;
    }

    public void setExpectedPattern(String expectedPattern) {
        this.expectedPattern = expectedPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FileSlot fileSlot = (FileSlot) o;
        return slot == fileSlot.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot);
    }

    @Override
    public String toString() {
        return "FileSlot{slot=" + slot + ", description='" + description + "'}";
    }
}
