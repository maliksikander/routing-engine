package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;

public interface MrdStateDelegate {
    Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState);
}
