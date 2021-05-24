package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.AgentMrdState;
import java.util.List;
import java.util.UUID;

public class AgentMrdStateChangedRequest {
    UUID agentId;
    List<AgentMrdState> agentMrdStates;

    public UUID getAgentId() {
        return agentId;
    }

    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    public List<AgentMrdState> getAgentMrdStates() {
        return agentMrdStates;
    }

    public void setAgentMrdStates(List<AgentMrdState> agentMrdStates) {
        this.agentMrdStates = agentMrdStates;
    }
}
