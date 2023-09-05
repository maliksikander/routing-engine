package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MrdType;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.MrdTypePool;

/**
 * The type Mrd state not ready.
 */
public class MrdStateNotReady implements MrdStateDelegate {
    private final MrdTypePool mrdTypePool;

    public MrdStateNotReady(MrdTypePool mrdTypePool) {
        this.mrdTypePool = mrdTypePool;
    }

    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        MrdType mrdType = this.mrdTypePool.getById(agentMrdState.getMrd().getType());

        if (!mrdType.isManagedByRe()) {
            return Enums.AgentMrdStateName.NOT_READY;
        }

        Enums.AgentMrdStateName currentState = agentMrdState.getState();
        String mrdId = agentMrdState.getMrd().getId();

        if (agent.getReservedTask() != null && agent.getReservedTask().getMrd().getId().equals(mrdId)) {
            return currentState;
        }

        if (currentState.equals(Enums.AgentMrdStateName.READY)
                || (currentState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)
                && agent.getNoOfActiveQueueTasks(agentMrdState.getMrd().getId()) < 1)) {
            return Enums.AgentMrdStateName.NOT_READY;
        } else if (currentState.equals(Enums.AgentMrdStateName.ACTIVE)
                || currentState.equals(Enums.AgentMrdStateName.BUSY)) {
            return Enums.AgentMrdStateName.PENDING_NOT_READY;
        }
        return currentState;
    }
}
