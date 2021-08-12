package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.commons.Enums;
import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * An AgentMrdStateChangeRequest object is used by the
 * {@link com.ef.mediaroutingengine.controllers.AgentStateController#mrdState(AgentMrdStateChangeRequest)
 * MRD-State-Change}** API as Request Body.
 */
public class AgentMrdStateChangeRequest {
    /**
     * Id of the agent, for which the MRD-state-change is requested.
     */
    @NotNull
    UUID agentId;
    /**
     * Agent's Associated MRD for which the state change is requested.
     */
    @NotNull
    UUID mrdId;
    /**
     * The New Agent-MRD-State requested.
     */
    @NotNull
    Enums.AgentMrdStateName state;

    /**
     * Default constructor for Jackson Serialization and deserialization.
     */
    public AgentMrdStateChangeRequest() {

    }

    /**
     * Parameterized Constructor.
     *
     * @param agentId Agent for which the MRD-state-change is requested
     * @param mrdId   MRD for which the state change is requested
     * @param state   new Agent-MRD-state requested
     */
    public AgentMrdStateChangeRequest(UUID agentId, UUID mrdId, Enums.AgentMrdStateName state) {
        this.agentId = agentId;
        this.mrdId = mrdId;
        this.state = state;
    }

    /**
     * Returns the value of agentId.
     *
     * @return the value of the agentId, an {@link UUID UUID} object or null
     */
    public UUID getAgentId() {
        return agentId;
    }

    /**
     * Sets the agentId field.
     *
     * @param agentId value to set
     */
    public void setAgentId(UUID agentId) {
        this.agentId = agentId;
    }

    /**
     * Returns the value of mrdId.
     *
     * @return the value of the mrdId, an {@link UUID UUID} object, or null
     */
    public UUID getMrdId() {
        return mrdId;
    }

    /**
     * Sets the mrdId field.
     *
     * @param mrdId value to set
     */
    public void setMrdId(UUID mrdId) {
        this.mrdId = mrdId;
    }

    /**
     * Returns the value of state.
     *
     * @return the value of state, an {@link Enums.AgentMrdStateName AgentMrdStateName} object, or null
     */
    public Enums.AgentMrdStateName getState() {
        return state;
    }

    /**
     * Sets the state field.
     *
     * @param state value of the state
     */
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
