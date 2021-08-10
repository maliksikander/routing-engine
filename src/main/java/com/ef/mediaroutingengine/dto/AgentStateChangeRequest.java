package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.AgentState;
import java.util.UUID;

/**
 * An AgentStateChangeRequest object is used by the
 * {@link com.ef.mediaroutingengine.controllers.AgentStateController#agentState(AgentStateChangeRequest)
 * Agent-State-Change}* API as Request Body.
 */
public class AgentStateChangeRequest {
    /**
     * Id of the agent for which the Agent-State change is requested.
     */
    UUID agentId;
    /**
     * The new Agent-State requested.
     */
    AgentState state;

    /**
     * Default Constructor for serialization and deserialization.
     */
    public AgentStateChangeRequest() {

    }

    /**
     * Parametrized Constructor.
     *
     * @param agentId UUID of the agent.
     * @param state   Agent-State of the agent.
     */
    public AgentStateChangeRequest(UUID agentId, AgentState state) {
        this.agentId = agentId;
        this.state = state;
    }

    /**
     * Gets agent id.
     *
     * @return the agent id
     */
    public UUID getAgentId() {
        return agentId;
    }

    /**
     * Sets agent id.
     *
     * @param agentId the agent id
     */
    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public AgentState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the state
     */
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