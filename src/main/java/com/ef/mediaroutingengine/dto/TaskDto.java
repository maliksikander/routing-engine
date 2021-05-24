package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import java.io.Serializable;
import java.util.UUID;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("task")
public class TaskDto implements Serializable {
    private UUID id;
    private MediaRoutingDomain mrd;
    private PrecisionQueue queue;
    private int priority;
    private Enums.TaskState state;
    private UUID assignedTo;

    public TaskDto() {

    }

    /**
     * Constructs object from Task object.
     *
     * @param task the object to construct from.
     */
    public TaskDto(Task task) {
        this.id = task.getId();
        this.mrd = task.getMrd();
        this.queue = task.getQueue();
        this.priority = task.getPriority();
        this.state = task.getTaskState();
        this.assignedTo = task.getAssignedTo();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    public PrecisionQueue getQueue() {
        return queue;
    }

    public void setQueue(PrecisionQueue queue) {
        this.queue = queue;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Enums.TaskState getState() {
        return state;
    }

    public void setState(Enums.TaskState state) {
        this.state = state;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    @Override
    public String toString() {
        return "TaskDto{"
                + "id=" + id
                + ", mrd=" + mrd
                + ", queue=" + queue
                + ", priority=" + priority
                + ", state=" + state
                + ", assignedTo=" + assignedTo
                + '}';
    }
}
