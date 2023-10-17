package com.ef.mediaroutingengine.routing.model;

/**
 * The interface Queue.
 */
public interface Queue {
    /**
     * Enqueue boolean.
     *
     * @param task the task
     * @return the boolean
     */
    boolean enqueue(QueueTask task);

    /**
     * Dequeue task.
     *
     * @return the task
     */
    QueueTask dequeue();

    /**
     * Print queue.
     */
    void printQueue();

    /**
     * Log all steps.
     */
    void logAllSteps();
}
