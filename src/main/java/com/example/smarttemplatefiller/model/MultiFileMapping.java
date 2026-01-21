package com.example.smarttemplatefiller.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Extended mapping that includes source file identifier for multi-file support.
 * Supports row-based, column-based, or mixed mapping strategies.
 */
public class MultiFileMapping {
    private String id;
    private int sourceFileSlot;
    private String sourceColumn;
    private String targetCell;
    private Direction direction;
    private boolean includeTitle;
    private String title;
    private java.util.Map<String, Object> rowPattern;
    private java.util.List<Integer> rowIndexes;

    public MultiFileMapping() {
        this.id = UUID.randomUUID().toString();
        this.direction = Direction.VERTICAL;
        this.includeTitle = false;
    }

    public MultiFileMapping(int sourceFileSlot, String sourceColumn, String targetCell, Direction direction) {
        this();
        this.sourceFileSlot = sourceFileSlot;
        this.sourceColumn = sourceColumn;
        this.targetCell = targetCell;
        this.direction = direction;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSourceFileSlot() {
        return sourceFileSlot;
    }

    public void setSourceFileSlot(int sourceFileSlot) {
        if (sourceFileSlot < 1 || sourceFileSlot > 10) {
            throw new IllegalArgumentException("Source file slot must be between 1 and 10");
        }
        this.sourceFileSlot = sourceFileSlot;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getTargetCell() {
        return targetCell;
    }

    public void setTargetCell(String targetCell) {
        this.targetCell = targetCell;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isIncludeTitle() {
        return includeTitle;
    }

    public void setIncludeTitle(boolean includeTitle) {
        this.includeTitle = includeTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public java.util.Map<String, Object> getRowPattern() {
        return rowPattern;
    }

    public void setRowPattern(java.util.Map<String, Object> rowPattern) {
        this.rowPattern = rowPattern;
    }

    public java.util.List<Integer> getRowIndexes() {
        return rowIndexes;
    }

    public void setRowIndexes(java.util.List<Integer> rowIndexes) {
        this.rowIndexes = rowIndexes;
    }

    /**
     * Returns a display string for the mapping list UI.
     * Format: "File1:ColA → A1"
     */
    public String toDisplayString() {
        return String.format("File%d:%s → %s", sourceFileSlot, sourceColumn, targetCell);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MultiFileMapping that = (MultiFileMapping) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MultiFileMapping{" +
                "sourceFileSlot=" + sourceFileSlot +
                ", sourceColumn='" + sourceColumn + '\'' +
                ", targetCell='" + targetCell + '\'' +
                ", direction=" + direction +
                '}';
    }
}
