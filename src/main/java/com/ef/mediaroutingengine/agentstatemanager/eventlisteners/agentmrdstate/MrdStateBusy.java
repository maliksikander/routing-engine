package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;


import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.routing.model.Agent;

/**
 * The type Mrd state busy.
 */
public class MrdStateBusy implements MrdStateDelegate {
    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        Enums.AgentMrdStateName currentState = agentMrdState.getState();
        if (currentState.equals(Enums.AgentMrdStateName.ACTIVE)
                || currentState.equals(Enums.AgentMrdStateName.READY)
                || currentState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)) {
            return Enums.AgentMrdStateName.BUSY;
        }
        return currentState;
    }
}
