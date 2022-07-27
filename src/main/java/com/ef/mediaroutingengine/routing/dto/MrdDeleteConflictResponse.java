package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.dto.TaskDto;
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
    private List<TaskDto> tasks;

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
                                     List<TaskDto> tasks) {
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
    public List<TaskDto> getTasks() {
        return tasks;
    }

    /**
     * Sets tasks.
     *
     * @param tasks the tasks
     */
    public void setTasks(List<TaskDto> tasks) {
        this.tasks = tasks;
    }
}
