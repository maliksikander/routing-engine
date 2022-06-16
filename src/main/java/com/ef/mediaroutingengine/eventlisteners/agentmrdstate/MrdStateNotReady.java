package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.model.Agent;

/**
 * The type Mrd state not ready.
 */
public class MrdStateNotReady implements MrdStateDelegate {

    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        Enums.AgentMrdStateName currentState = agentMrdState.getState();
        if (currentState.equals(Enums.AgentMrdStateName.READY)
                || (currentState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)
                && agent.getNoOfActivePushTasks(agentMrdState.getMrd().getId()) < 1)) {
            return Enums.AgentMrdStateName.NOT_READY;
        } else if (currentState.equals(Enums.AgentMrdStateName.ACTIVE)
                || currentState.equals(Enums.AgentMrdStateName.BUSY)) {
            return Enums.AgentMrdStateName.PENDING_NOT_READY;
        }
        return currentState;
    }
}
