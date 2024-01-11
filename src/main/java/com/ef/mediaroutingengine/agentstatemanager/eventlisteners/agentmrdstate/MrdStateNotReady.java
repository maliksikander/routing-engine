package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.MrdPool;

/**
 * The type Mrd state not ready.
 */
public class MrdStateNotReady implements MrdStateDelegate {
    private final MrdPool mrdPool;

    public MrdStateNotReady(MrdPool mrdPool) {
        this.mrdPool = mrdPool;
    }

    @Override
    public Enums.AgentMrdStateName getNewState(Agent agent, AgentMrdState agentMrdState) {
        if (!this.mrdPool.getType(agentMrdState.getMrd().getId()).isManagedByRe()) {
            return Enums.AgentMrdStateName.NOT_READY;
        }

        Enums.AgentMrdStateName currentState = agentMrdState.getState();
        String mrdId = agentMrdState.getMrd().getId();

        if (agent.getReservedTask() != null && agent.getReservedTask().getMrdId().equals(mrdId)) {
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
