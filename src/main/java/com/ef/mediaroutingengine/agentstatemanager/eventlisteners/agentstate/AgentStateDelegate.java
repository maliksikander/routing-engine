package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentState;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.routing.model.Agent;

/**
 * The interface Agent state delegate.
 */
public interface AgentStateDelegate {
    /**
     * Update state boolean.
     *
     * @param agent    the agent
     * @param newState the new state
     * @return the boolean
     */
    AgentStateChangedResponse updateState(Agent agent, AgentState newState, boolean isChangedInternally);
}
