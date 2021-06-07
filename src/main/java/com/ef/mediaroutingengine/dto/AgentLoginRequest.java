package com.ef.mediaroutingengine.dto;

import java.util.UUID;

public class AgentLoginRequest {
    private UUID agentId;

    public UUID getAgentId() {
        return agentId;
    }

    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return "AgentLoginRequest{"
                + "agentId=" + agentId
                + '}';
    }
}
