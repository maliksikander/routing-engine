package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentState;

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
    boolean updateState(Agent agent, AgentState newState);
}
