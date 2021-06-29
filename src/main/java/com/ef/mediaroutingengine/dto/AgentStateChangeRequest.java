package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.AgentState;
import java.util.UUID;

public class AgentStateChangeRequest {
    UUID agentId;
    AgentState state;

    public AgentStateChangeRequest() {

    }

    public AgentStateChangeRequest(UUID agentId, AgentState state) {
        this.agentId = agentId;
        this.state = state;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "AgentStateChangeRequest{"
                + "agentId=" + agentId
                + ", state=" + state
                + '}';
    }
}
