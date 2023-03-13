package com.ef.mediaroutingengine.routing.model;

import com.ef.mediaroutingengine.taskmanager.model.Task;

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
