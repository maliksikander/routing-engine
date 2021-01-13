package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.Agent;

public interface AgentStateManager {
    boolean changeState(Agent agent);
}
