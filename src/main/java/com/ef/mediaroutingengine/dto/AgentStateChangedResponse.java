package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.AgentPresence;
import java.io.Serializable;

public class AgentStateChangedResponse implements Serializable {
    private AgentPresence agentPresence;
    private boolean agentStateChanged;

    public AgentStateChangedResponse() {

    }

    public AgentStateChangedResponse(AgentPresence agentPresence, boolean agentStateChanged) {
        this.agentPresence = agentPresence;
        this.agentStateChanged = agentStateChanged;
    }

    public AgentPresence getAgentPresence() {
        return agentPresence;
    }

    public void setAgentPresence(AgentPresence agentPresence) {
        this.agentPresence = agentPresence;
    }

    public boolean isAgentStateChanged() {
        return agentStateChanged;
    }

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
