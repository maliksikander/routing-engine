package com.ef.mediaroutingengine.model;

/**
 * The interface Queue.
 */
public interface IQueue {
    /**
     * Enqueue boolean.
     *
     * @param task the task
     * @return the boolean
     */
    boolean enqueue(Task task);

    /**
     * Dequeue task.
     *
     * @return the task
     */
    Task dequeue();

    /**
     * Print queue.
     */
    void printQueue();

    /**
     * Log all steps.
     */
    void logAllSteps();
}
