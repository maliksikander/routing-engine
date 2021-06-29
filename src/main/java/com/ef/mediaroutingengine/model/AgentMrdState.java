package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.commons.Enums;
import java.time.LocalDateTime;

public class AgentMrdState {
    private MediaRoutingDomain mrd;
    private Enums.AgentMrdStateName state;
    private LocalDateTime stateChangeTime;
    private LocalDateTime lastReadyStateChangeTime;

    public AgentMrdState() {

    }

    /**
     * Parameterized Constructor.
     *
     * @param mrd   id the associated mrd
     * @param state agent's state for the associated mrd
     */
    public AgentMrdState(MediaRoutingDomain mrd, Enums.AgentMrdStateName state) {
        this.mrd = mrd;
        this.state = state;
        this.stateChangeTime = LocalDateTime.now();
        this.lastReadyStateChangeTime = LocalDateTime.of(1990, 4, 2, 12, 1);
    }

    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    public Enums.AgentMrdStateName getState() {
        return state;
    }

    /**
     * Sets the state and the state change time to current time. If state is READY, it sets the
     * last-ready-state-change-time to current time.
     *
     * @param state state to set.
     */
    public void setState(Enums.AgentMrdStateName state) {
        this.state = state;
        this.stateChangeTime = LocalDateTime.now();
        if (this.state.equals(Enums.AgentMrdStateName.READY)) {
            this.lastReadyStateChangeTime = this.stateChangeTime;
        }
    }

    public LocalDateTime getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(LocalDateTime stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    public LocalDateTime getLastReadyStateChangeTime() {
        return lastReadyStateChangeTime;
    }

    public void setLastReadyStateChangeTime(LocalDateTime lastReadyStateChangeTime) {
        this.lastReadyStateChangeTime = lastReadyStateChangeTime;
    }

    @Override
    public String toString() {
        return "AgentMrdState{"
                + "mrd=" + mrd
                + ", state=" + state
                + ", stateChangeTime=" + stateChangeTime
                + ", lastReadyStateChangeTime=" + lastReadyStateChangeTime
                + '}';
    }
}
