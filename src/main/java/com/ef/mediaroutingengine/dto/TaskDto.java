package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import java.io.Serializable;
import java.util.UUID;

/**
 * The type Task dto.
 */
public class TaskDto implements Serializable {
    /**
     * The ID.
     */
    private UUID id;
    /**
     * The Channel session.
     */
    private ChannelSession channelSession;
    /**
     * The Mrd.
     */
    private MediaRoutingDomain mrd;
    /**
     * The Queue.
     */
    private String queue;
    /**
     * The Priority.
     */
    private int priority;
    /**
     * The State.
     */
    private TaskState state;
    /**
     * The Assigned to.
     */
    private UUID assignedTo;
    /**
     * The Enqueue time.
     */
    private Long enqueueTime;

    /**
     * Instantiates a new Task dto.
     */
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

    /**
     * Gets id.
     *
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets mrd.
     *
     * @return the mrd
     */
    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    /**
     * Sets mrd.
     *
     * @param mrd the mrd
     */
    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    /**
     * Gets queue.
     *
     * @return the queue
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Sets queue.
     *
     * @param queue the queue
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
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

    /**
     * Gets assigned to.
     *
     * @return the assigned to
     */
    public UUID getAssignedTo() {
        return assignedTo;
    }

    /**
     * Sets assigned to.
     *
     * @param assignedTo the assigned to
     */
    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Gets channel session.
     *
     * @return the channel session
     */
    public ChannelSession getChannelSession() {
        return channelSession;
    }

    /**
     * Sets channel session.
     *
     * @param channelSession the channel session
     */
    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }

    /**
     * Gets enqueue time.
     *
     * @return the enqueue time
     */
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
