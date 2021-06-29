package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.mediaroutingengine.commons.EFUtils;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;

public class MrdStateReady implements MrdStateDelegate {

    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        Enums.AgentMrdStateName currentState = agentMrdState.getState();

        if (agent.getState().getName().equals(Enums.AgentStateName.READY)) {
            if (currentState.equals(Enums.AgentMrdStateName.NOT_READY)
                    || (currentState.equals(Enums.AgentMrdStateName.ACTIVE)
                    && agent.getTasksCountFor(agentMrdState.getMrd().getId()) < 1)) {
                return Enums.AgentMrdStateName.READY;
            } else if (currentState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)) {
                if (agent.getTasksCountFor(agentMrdState.getMrd().getId()) < 1) {
                    return Enums.AgentMrdStateName.READY;
                } else if (agent.getTasksCountFor(agentMrdState.getMrd().getId()) == EFUtils.MAX_TASKS) {
                    return Enums.AgentMrdStateName.BUSY;
                } else {
                    return Enums.AgentMrdStateName.ACTIVE;
                }
            }
        }
        return currentState;
    }
}
