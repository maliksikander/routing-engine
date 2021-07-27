package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.TaskState;
import java.io.Serializable;
import java.util.UUID;

/**
 * The type Task state change request.
 */
public class TaskStateChangeRequest implements Serializable {
    /**
     * The Task id.
     */
    private UUID taskId;
    /**
     * The State.
     */
    private TaskState state;

    /**
     * Instantiates a new Task state change request.
     */
    public TaskStateChangeRequest() {

    }

    /**
     * Instantiates a new Task state change request.
     *
     * @param taskId the task id
     * @param state  the state
     */
    public TaskStateChangeRequest(UUID taskId, TaskState state) {
        this.taskId = taskId;
        this.state = state;
    }

    /**
     * Gets task id.
     *
     * @return the task id
     */
    public UUID getTaskId() {
        return taskId;
    }

    /**
     * Sets task id.
     *
     * @param taskId the task id
     */
    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public TaskState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the state
     */
    public void setState(TaskState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "TaskStateChangeRequest{"
                + "taskId=" + taskId
                + ", state=" + state
                + '}';
    }
}
