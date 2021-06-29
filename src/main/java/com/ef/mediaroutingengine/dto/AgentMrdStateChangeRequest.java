package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.commons.Enums;
import java.util.UUID;

public class AgentMrdStateChangeRequest {
    UUID agentId;
    UUID mrdId;
    Enums.AgentMrdStateName state;

    /**
     * Default constructor for Jackson Serialization and deserialization.
     */
    public AgentMrdStateChangeRequest() {

    }

    /**
     * Parameterized Constructor.
     *
     * @param agentId id of the agent.
     * @param mrdId   id of the mrd
     * @param state   requested Agent MRD state for the state change
     */
    public AgentMrdStateChangeRequest(UUID agentId, UUID mrdId, Enums.AgentMrdStateName state) {
        this.agentId = agentId;
        this.mrdId = mrdId;
        this.state = state;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

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

    @Override
    public String toString() {
        return "AgentMrdStateChangeRequest{"
                + "agentId=" + agentId
                + ", mrdId=" + mrdId
                + ", state=" + state
                + '}';
    }
}
