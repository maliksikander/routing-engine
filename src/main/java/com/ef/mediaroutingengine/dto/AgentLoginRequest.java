package com.ef.mediaroutingengine.dto;

import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * An AgentLoginRequest object is used by the
 * {@link com.ef.mediaroutingengine.controllers.AgentStateController#agentLogin(AgentLoginRequest) Agent-Login}
 * API as Request Body.
 */
public class AgentLoginRequest {
    /**
     * Id of the agent to login.
     */
    @NotNull
    private UUID agentId;

    /**
     * Returns the value of the agentId.
     *
     * @return the value of the agentId, an {@link UUID UUID} object, or null
     */
    public UUID getAgentId() {
        return agentId;
    }

    /**
     * Sets the agentId field.
     *
     * @param agentId value of the agentId
     */
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
