package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Task;
import java.util.List;

/**
 * The type Mrd delete conflict response.
 */
public class MrdDeleteConflictResponse {
    /**
     * The Precision queues.
     */
    private List<PrecisionQueueEntity> precisionQueues;
    /**
     * The Tasks.
     */
    private List<Task> tasks;

    /**
     * Instantiates a new Mrd delete conflict response.
     */
    public MrdDeleteConflictResponse() {

    }

    /**
     * Instantiates a new Mrd delete conflict response.
     *
     * @param precisionQueues the precision queues
     * @param tasks           the tasks
     */
    public MrdDeleteConflictResponse(List<PrecisionQueueEntity> precisionQueues,
                                     List<Task> tasks) {
        this.precisionQueues = precisionQueues;
        this.tasks = tasks;
    }

    /**
     * Gets precision queues.
     *
     * @return the precision queues
     */
    public List<PrecisionQueueEntity> getPrecisionQueues() {
        return precisionQueues;
    }

    /**
     * Sets precision queues.
     *
     * @param precisionQueues the precision queues
     */
    public void setPrecisionQueues(List<PrecisionQueueEntity> precisionQueues) {
        this.precisionQueues = precisionQueues;
    }

    /**
     * Gets tasks.
     *
     * @return the tasks
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Sets tasks.
     *
     * @param tasks the tasks
     */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
