package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import java.io.Serializable;
import java.util.UUID;

public class TaskDto implements Serializable {
    private UUID id;
    private ChannelSession channelSession;
    private MediaRoutingDomain mrd;
    private UUID queue;
    private int priority;
    private TaskState state;
    private UUID assignedTo;
    private Long enqueueTime;

    public TaskDto() {

    }

    /**
     * Constructs object from Task object.
     *
     * @param task the object to construct from.
     */
    public TaskDto(Task task) {
        this.id = task.getId();
        this.channelSession = task.getChannelSession();
        this.mrd = task.getMrd();
        this.queue = task.getQueue();
        this.priority = task.getPriority();
        this.state = task.getTaskState();
        this.assignedTo = task.getAssignedTo();
        this.enqueueTime = task.getEnqueueTime();
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

    public UUID getQueue() {
        return queue;
    }

    public void setQueue(UUID queue) {
        this.queue = queue;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public ChannelSession getChannelSession() {
        return channelSession;
    }

    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }

    public Long getEnqueueTime() {
        return enqueueTime;
    }

    @Override
    public String toString() {
        return "TaskDto{"
                + "id=" + id
                + ", channelSession=" + channelSession
                + ", mrd=" + mrd
                + ", queue=" + queue
                + ", priority=" + priority
                + ", state=" + state
                + ", assignedTo=" + assignedTo
                + '}';
    }
}
