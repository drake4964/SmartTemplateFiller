package com.example.smarttemplatefiller;

import java.util.ArrayList;
import java.util.List;

/**
 * Result class for append operations with success/failure status,
 * row counts, and any warnings or errors.
 */
public class AppendResult {

    private final boolean success;
    private final int rowsAdded;
    private final int rowOffset;
    private final String targetFilePath;
    private final List<String> warnings;
    private final String errorMessage;

    /**
     * Private constructor - use factory methods.
     */
    private AppendResult(boolean success, int rowsAdded, int rowOffset,
            String targetFilePath, List<String> warnings, String errorMessage) {
        this.success = success;
        this.rowsAdded = rowsAdded;
        this.rowOffset = rowOffset;
        this.targetFilePath = targetFilePath;
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
        this.errorMessage = errorMessage;
    }

    /**
     * Create a successful result.
     */
    public static AppendResult success(int rowsAdded, int rowOffset, String targetFilePath) {
        return new AppendResult(true, rowsAdded, rowOffset, targetFilePath, new ArrayList<>(), null);
    }

    /**
     * Create a successful result with warnings.
     */
    public static AppendResult successWithWarnings(int rowsAdded, int rowOffset,
            String targetFilePath, List<String> warnings) {
        return new AppendResult(true, rowsAdded, rowOffset, targetFilePath, warnings, null);
    }

    /**
     * Create a failure result.
     */
    public static AppendResult failure(String errorMessage) {
        return new AppendResult(false, 0, 0, null, new ArrayList<>(), errorMessage);
    }

    /**
     * Create a failure result with partial info.
     */
    public static AppendResult failure(String errorMessage, String targetFilePath) {
        return new AppendResult(false, 0, 0, targetFilePath, new ArrayList<>(), errorMessage);
    }

    // Getters

    public boolean isSuccess() {
        return success;
    }

    public int getRowsAdded() {
        return rowsAdded;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public String getTargetFilePath() {
        return targetFilePath;
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("AppendResult[success, rowsAdded=%d, offset=%d, target=%s, warnings=%d]",
                    rowsAdded, rowOffset, targetFilePath, warnings.size());
        } else {
            return String.format("AppendResult[failed: %s]", errorMessage);
        }
    }
}
