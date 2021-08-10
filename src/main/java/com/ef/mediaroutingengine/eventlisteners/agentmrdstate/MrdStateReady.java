package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;

/**
 * The type Mrd state ready.
 */
public class MrdStateReady implements MrdStateDelegate {

    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        Enums.AgentMrdStateName currentState = agentMrdState.getState();

        if (agent.getState().getName().equals(Enums.AgentStateName.READY)) {
            if (currentState.equals(Enums.AgentMrdStateName.NOT_READY)
                    || (currentState.equals(Enums.AgentMrdStateName.ACTIVE)
                    && agent.getNoOfActiveTasks(agentMrdState.getMrd().getId()) < 1)) {
                return Enums.AgentMrdStateName.READY;
            } else if (currentState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)) {
                if (agent.getNoOfActiveTasks(agentMrdState.getMrd().getId()) < 1) {
                    return Enums.AgentMrdStateName.READY;
                } else if (agent.getNoOfActiveTasks(agentMrdState.getMrd().getId()) == Constants.MAX_TASKS) {
                    return Enums.AgentMrdStateName.BUSY;
                } else {
                    return Enums.AgentMrdStateName.ACTIVE;
                }
            }
        }
        return currentState;
    }
}