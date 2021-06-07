package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.Enums;
import java.util.UUID;

public class AgentStateChangeRequest {
    UUID agentId;
    Enums.AgentStateName state;

    public UUID getAgentId() {
        return agentId;
    }

    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    public Enums.AgentStateName getState() {
        return state;
    }

    public void setState(Enums.AgentStateName state) {
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
