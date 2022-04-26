package com.ef.mediaroutingengine.dto;

/**
 * The type Queue agent stats.
 */
public class QueueAgentStats {
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

    /**
     * Instantiates a new Queue agent stats.
     *
     * @param mrdName the mrd name
     */
    public QueueAgentStats(String mrdName) {
        this.mrdName = mrdName;
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
     * Incr not ready agents.
     */
    public void incrNotReadyAgents() {
        this.notReadyAgents++;
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
     * Incr ready agents.
     */
    public void incrReadyAgents() {
        this.readyAgents++;
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
     * Incr active agents.
     */
    public void incrActiveAgents() {
        this.activeAgents++;
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
     * Incr pending not ready agents.
     */
    public void incrPendingNotReadyAgents() {
        this.pendingNotReadyAgents++;
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
     * Incr busy agents.
     */
    public void incrBusyAgents() {
        this.busyAgents++;
    }
}
