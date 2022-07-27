package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.routing.model.Agent;

/**
 * The type Mrd state ready.
 */
public class MrdStateReady implements MrdStateDelegate {

    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        Enums.AgentMrdStateName currentState = agentMrdState.getState();

        if (agent.getState().getName().equals(Enums.AgentStateName.READY)) {
            if (currentState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)
                    && agentMrdState.getMaxAgentTasks() == 0) {
                return Enums.AgentMrdStateName.BUSY;
            } else if (agentMrdState.getMaxAgentTasks() == 0) {
                return Enums.AgentMrdStateName.NOT_READY;
            } else if (currentState.equals(Enums.AgentMrdStateName.NOT_READY)
                    || currentState.equals(Enums.AgentMrdStateName.BUSY)
                    || (currentState.equals(Enums.AgentMrdStateName.ACTIVE)
                    && agent.getNoOfActivePushTasks(agentMrdState.getMrd().getId()) < 1)) {
                return Enums.AgentMrdStateName.READY;
            } else if (currentState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)) {
                if (agent.getNoOfActivePushTasks(agentMrdState.getMrd().getId()) < 1) {
                    return Enums.AgentMrdStateName.READY;
                } else if (agent.getNoOfActivePushTasks(agentMrdState.getMrd().getId())
                        == agentMrdState.getMaxAgentTasks()) {
                    return Enums.AgentMrdStateName.BUSY;
                } else {
                    return Enums.AgentMrdStateName.ACTIVE;
                }
            }
        }
        return currentState;
    }
}