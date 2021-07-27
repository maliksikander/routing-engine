package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.AgentPresence;
import java.io.Serializable;

/**
 * An AgentStateChangedResponse object is used as a DTO to publish changes in the Agent / Agent-MRD states
 * to the Agent-Manager.
 */
public class AgentStateChangedResponse implements Serializable {
    /**
     * Contains the information of the Agent and it's states.
     */
    private AgentPresence agentPresence;
    /**
     * True if and only if the Agent-State is changed.
     */
    private boolean agentStateChanged;

    /**
     * Default Constructor for serialization and deserialization.
     */
    public AgentStateChangedResponse() {

    }

    /**
     * Parameterized Constructor.
     *
     * @param agentPresence     AgentPresence Object
     * @param agentStateChanged tells whether the Agent-State is changed
     */
    public AgentStateChangedResponse(AgentPresence agentPresence, boolean agentStateChanged) {
        this.agentPresence = agentPresence;
        this.agentStateChanged = agentStateChanged;
    }

    /**
     * Gets agent presence.
     *
     * @return the agent presence
     */
    public AgentPresence getAgentPresence() {
        return agentPresence;
    }

    /**
     * Sets agent presence.
     *
     * @param agentPresence the agent presence
     */
    public void setAgentPresence(AgentPresence agentPresence) {
        this.agentPresence = agentPresence;
    }

    /**
     * Is agent state changed boolean.
     *
     * @return the boolean
     */
    public boolean isAgentStateChanged() {
        return agentStateChanged;
    }

    /**
     * Sets agent state changed.
     *
     * @param agentStateChanged the agent state changed
     */
    public void setAgentStateChanged(boolean agentStateChanged) {
        this.agentStateChanged = agentStateChanged;
    }

    @Override
    public String toString() {
        return "AgentStateChangedResponse{"
                + "agentPresence=" + agentPresence
                + ", agentStateChanged=" + agentStateChanged
                + '}';
    }
}
