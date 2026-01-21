package com.example.smarttemplatefiller.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an auto-processing task.
 * Tracks the status and results of multi-file merge jobs.
 */
public class ProcessingJob {
    private String id;
    private JobStatus status;
    private String matchedPrefix;
    private Map<Integer, Path> inputFiles;
    private Path outputFile;
    private Path archiveFolder;
    private Instant startTime;
    private Instant endTime;
    private String errorMessage;

    public ProcessingJob() {
        this.id = UUID.randomUUID().toString();
        this.status = JobStatus.PENDING;
        this.inputFiles = new HashMap<>();
    }

    public ProcessingJob(String matchedPrefix) {
        this();
        this.matchedPrefix = matchedPrefix;
    }

    // State transition methods
    public void startProcessing() {
        this.status = JobStatus.PROCESSING;
        this.startTime = Instant.now();
    }

    public void complete(Path outputFile, Path archiveFolder) {
        this.status = JobStatus.COMPLETED;
        this.outputFile = outputFile;
        this.archiveFolder = archiveFolder;
        this.endTime = Instant.now();
    }

    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getMatchedPrefix() {
        return matchedPrefix;
    }

    public void setMatchedPrefix(String matchedPrefix) {
        this.matchedPrefix = matchedPrefix;
    }

    public Map<Integer, Path> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(Map<Integer, Path> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void addInputFile(int slot, Path file) {
        this.inputFiles.put(slot, file);
    }

    public Path getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(Path outputFile) {
        this.outputFile = outputFile;
    }

    public Path getArchiveFolder() {
        return archiveFolder;
    }

    public void setArchiveFolder(Path archiveFolder) {
        this.archiveFolder = archiveFolder;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get processing duration in milliseconds.
     * Returns -1 if not yet completed.
     */
    public long getDurationMs() {
        if (startTime == null || endTime == null) {
            return -1;
        }
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProcessingJob that = (ProcessingJob) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProcessingJob{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", matchedPrefix='" + matchedPrefix + '\'' +
                ", inputFiles=" + inputFiles.size() +
                '}';
    }
}
