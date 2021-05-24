package com.ef.mediaroutingengine.model;

import java.sql.Timestamp;
import java.util.UUID;

public class AgentMrdState {
    private UUID mrdId;
    private Enums.AgentMrdStateName state;
    private Timestamp stateChangeTime;

    public UUID getMrdId() {
        return mrdId;
    }

    public void setMrdId(UUID mrdId) {
        this.mrdId = mrdId;
    }

    public Enums.AgentMrdStateName getState() {
        return state;
    }

    public void setState(Enums.AgentMrdStateName state) {
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
