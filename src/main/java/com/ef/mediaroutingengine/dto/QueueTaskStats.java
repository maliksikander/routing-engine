package com.ef.mediaroutingengine.dto;

/**
 * The type Queue task stats.
 */
public class QueueTaskStats {
    /**
     * The Total queued.
     */
    private int totalQueued;
    /**
     * The Longest in queue.
     */
    private long longestInQueue;
    /**
     * The Total active.
     */
    private int totalActive;

    /**
     * Instantiates a new Queue task stats.
     */
    public QueueTaskStats() {
        this.longestInQueue = Long.MAX_VALUE;
    }

    /**
     * Gets total queued.
     *
     * @return the total queued
     */
    public int getTotalQueued() {
        return totalQueued;
    }

    /**
     * Incr total queued.
     */
    public void incrTotalQueued() {
        this.totalQueued++;
    }

    /**
     * Gets longest in queue.
     *
     * @return the longest in queue
     */
    public long getLongestInQueue() {
        return longestInQueue;
    }

    /**
     * Sets longest in queue.
     *
     * @param longestInQueue the longest in queue
     */
    public void setLongestInQueue(long longestInQueue) {
        this.longestInQueue = longestInQueue;
    }

    /**
     * Gets total active.
     *
     * @return the total active
     */
    public int getTotalActive() {
        return totalActive;
    }

    /**
     * Incr total active.
     */
    public void incrTotalActive() {
        this.totalActive++;
    }
}
