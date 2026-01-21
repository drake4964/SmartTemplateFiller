package com.example.smarttemplatefiller.model;

/**
 * Configuration for folder watching behavior.
 */
public class WatchConfiguration {
    private int stabilityCheckSeconds;
    private MatchingStrategy matchingStrategy;

    public WatchConfiguration() {
        this.stabilityCheckSeconds = 2; // Default 2 seconds
        this.matchingStrategy = MatchingStrategy.PREFIX;
    }

    public WatchConfiguration(int stabilityCheckSeconds, MatchingStrategy matchingStrategy) {
        this.stabilityCheckSeconds = stabilityCheckSeconds;
        this.matchingStrategy = matchingStrategy;
    }

    // Getters and Setters
    public int getStabilityCheckSeconds() {
        return stabilityCheckSeconds;
    }

    public void setStabilityCheckSeconds(int stabilityCheckSeconds) {
        if (stabilityCheckSeconds < 1 || stabilityCheckSeconds > 30) {
            throw new IllegalArgumentException("Stability check must be between 1 and 30 seconds");
        }
        this.stabilityCheckSeconds = stabilityCheckSeconds;
    }

    public MatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    public void setMatchingStrategy(MatchingStrategy matchingStrategy) {
        this.matchingStrategy = matchingStrategy;
    }

    @Override
    public String toString() {
        return "WatchConfiguration{" +
                "stabilityCheckSeconds=" + stabilityCheckSeconds +
                ", matchingStrategy=" + matchingStrategy +
                '}';
    }
}
