package com.example.smarttemplatefiller.model;

/**
 * Strategy for matching files across watched folders.
 */
public enum MatchingStrategy {
    /** Match by text before first underscore */
    PREFIX,
    /** Match by exact filename (minus extension) */
    EXACT_BASENAME
}
