package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.TaskState;
import java.io.Serializable;
import java.util.UUID;

public class TaskStateChangeRequest implements Serializable {
    private UUID taskId;
    private TaskState state;

    public TaskStateChangeRequest() {

    }

    public TaskStateChangeRequest(UUID taskId, TaskState state) {
        this.taskId = taskId;
        this.state = state;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public TaskState getState() {
        return state;
    }

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
