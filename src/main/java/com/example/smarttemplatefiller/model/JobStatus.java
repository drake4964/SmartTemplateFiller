package com.example.smarttemplatefiller.model;

/**
 * Status of a processing job.
 */
public enum JobStatus {
    /** Files matched, waiting to process */
    PENDING,
    /** Currently generating output */
    PROCESSING,
    /** Successfully created output */
    COMPLETED,
    /** Error occurred during processing */
    FAILED
}
