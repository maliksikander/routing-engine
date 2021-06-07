package com.ef.mediaroutingengine.model;

import java.sql.Timestamp;
import java.util.UUID;

public class AgentMrdState {
    private UUID mrdId;
    private MrdState state;
    private Timestamp stateChangeTime;

    public AgentMrdState() {

    }

    /**
     * Parameterized Constructor.
     * @param mrdId id the associated mrd
     * @param state agent's state for the associated mrd
     */
    public AgentMrdState(UUID mrdId, MrdState state) {
        this.mrdId = mrdId;
        this.state = state;
        this.stateChangeTime = new Timestamp(System.currentTimeMillis());
    }

    public UUID getMrdId() {
        return mrdId;
    }

    public void setMrdId(UUID mrdId) {
        this.mrdId = mrdId;
    }

    public MrdState getState() {
        return state;
    }

    public void setState(MrdState state) {
        this.state = state;
    }

    public Timestamp getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(Timestamp stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    @Override
    public String toString() {
        return "AgentMrdState{"
                + "mrdId=" + mrdId
                + ", state=" + state
                + ", stateChangeTime=" + stateChangeTime
                + '}';
    }
}
