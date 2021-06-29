package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentState;

public interface AgentStateDelegate {
    boolean updateState(Agent agent, AgentState newState);
}
