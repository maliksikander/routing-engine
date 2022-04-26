package com.ef.mediaroutingengine.dto;

/**
 * The type Queue stats dto.
 */
public class QueueStatsDto {
    /**
     * The Name.
     */
    private String name;
    /**
     * The Total queued.
     */
    private int totalQueued;
    /**
     * The Longest in queue.
     */
    private long longestInQueue;
    /**
     * The Total active.
     */
    private int totalActive;
    /**
     * The Mrd name.
     */
    private String mrdName;
    /**
     * The Not ready agents.
     */
    private int notReadyAgents;
    /**
     * The Ready agents.
     */
    private int readyAgents;
    /**
     * The Active agents.
     */
    private int activeAgents;
    /**
     * The Pending not ready agents.
     */
    private int pendingNotReadyAgents;
    /**
     * The Busy agents.
     */
    private int busyAgents;

    public QueueStatsDto() {

    }

    /**
     * Instantiates a new Queue stats dto.
     *
     * @param name            the name
     * @param queueTaskStats  the queue task stats
     * @param queueAgentStats the queue agent stats
     */
    public QueueStatsDto(String name, QueueTaskStats queueTaskStats, QueueAgentStats queueAgentStats) {
        this.name = name;
        this.totalQueued = queueTaskStats.getTotalQueued();
        this.longestInQueue = queueTaskStats.getLongestInQueue();
        this.totalActive = queueTaskStats.getTotalActive();
        this.mrdName = queueAgentStats.getMrdName();
        this.notReadyAgents = queueAgentStats.getNotReadyAgents();
        this.readyAgents = queueAgentStats.getReadyAgents();
        this.activeAgents = queueAgentStats.getActiveAgents();
        this.pendingNotReadyAgents = queueAgentStats.getPendingNotReadyAgents();
        this.busyAgents = queueAgentStats.getBusyAgents();
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets total queued.
     *
     * @return the total queued
     */
    public int getTotalQueued() {
        return totalQueued;
    }

    /**
     * Sets total queued.
     *
     * @param totalQueued the total queued
     */
    public void setTotalQueued(int totalQueued) {
        this.totalQueued = totalQueued;
    }

    /**
     * Gets longest in queue.
     *
     * @return the longest in queue
     */
    public long getLongestInQueue() {
        return longestInQueue;
    }

    /**
     * Sets longest in queue.
     *
     * @param longestInQueue the longest in queue
     */
    public void setLongestInQueue(long longestInQueue) {
        this.longestInQueue = longestInQueue;
    }

    /**
     * Gets total active.
     *
     * @return the total active
     */
    public int getTotalActive() {
        return totalActive;
    }

    /**
     * Sets total active.
     *
     * @param totalActive the total active
     */
    public void setTotalActive(int totalActive) {
        this.totalActive = totalActive;
    }

    /**
     * Gets mrd name.
     *
     * @return the mrd name
     */
    public String getMrdName() {
        return mrdName;
    }

    /**
     * Sets mrd name.
     *
     * @param mrdName the mrd name
     */
    public void setMrdName(String mrdName) {
        this.mrdName = mrdName;
    }

    /**
     * Gets not ready agents.
     *
     * @return the not ready agents
     */
    public int getNotReadyAgents() {
        return notReadyAgents;
    }

    /**
     * Sets not ready agents.
     *
     * @param notReadyAgents the not ready agents
     */
    public void setNotReadyAgents(int notReadyAgents) {
        this.notReadyAgents = notReadyAgents;
    }

    /**
     * Gets ready agents.
     *
     * @return the ready agents
     */
    public int getReadyAgents() {
        return readyAgents;
    }

    /**
     * Sets ready agents.
     *
     * @param readyAgents the ready agents
     */
    public void setReadyAgents(int readyAgents) {
        this.readyAgents = readyAgents;
    }

    /**
     * Gets active agents.
     *
     * @return the active agents
     */
    public int getActiveAgents() {
        return activeAgents;
    }

    /**
     * Sets active agents.
     *
     * @param activeAgents the active agents
     */
    public void setActiveAgents(int activeAgents) {
        this.activeAgents = activeAgents;
    }

    /**
     * Gets pending not ready agents.
     *
     * @return the pending not ready agents
     */
    public int getPendingNotReadyAgents() {
        return pendingNotReadyAgents;
    }

    /**
     * Sets pending not ready agents.
     *
     * @param pendingNotReadyAgents the pending not ready agents
     */
    public void setPendingNotReadyAgents(int pendingNotReadyAgents) {
        this.pendingNotReadyAgents = pendingNotReadyAgents;
    }

    /**
     * Gets busy agents.
     *
     * @return the busy agents
     */
    public int getBusyAgents() {
        return busyAgents;
    }

    /**
     * Sets busy agents.
     *
     * @param busyAgents the busy agents
     */
    public void setBusyAgents(int busyAgents) {
        this.busyAgents = busyAgents;
    }
}
