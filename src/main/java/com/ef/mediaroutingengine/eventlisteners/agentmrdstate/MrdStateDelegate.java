package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.model.Agent;

/**
 * The interface Mrd state delegate.
 */
public interface MrdStateDelegate {
    /**
     * Gets new state.
     *
     * @param agent         the agent
     * @param agentMrdState the agent mrd state
     * @return the new state
     */
    Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState);
}
