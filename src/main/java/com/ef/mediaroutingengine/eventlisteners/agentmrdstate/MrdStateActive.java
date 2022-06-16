package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;


import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.model.Agent;

/**
 * The type Mrd state active.
 */
public class MrdStateActive implements MrdStateDelegate {

    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        Enums.AgentMrdStateName currentState = agentMrdState.getState();
        if (currentState.equals(Enums.AgentMrdStateName.READY)
                || currentState.equals(Enums.AgentMrdStateName.BUSY)) {
            return Enums.AgentMrdStateName.ACTIVE;
        }
        return currentState;
    }
}
