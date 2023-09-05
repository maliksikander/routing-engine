package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MrdType;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.MrdTypePool;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Agent state not ready.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateNotReady implements AgentStateDelegate {
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;
    private final MrdTypePool mrdTypePool;

    /**
     * Instantiates a new Agent state not ready.
     *
     * @param agentPresenceRepository the agent presence repository
     */
    public AgentStateNotReady(AgentPresenceRepository agentPresenceRepository, MrdTypePool mrdTypePool) {
        this.agentPresenceRepository = agentPresenceRepository;
        this.mrdTypePool = mrdTypePool;
    }

    @Override
    public AgentStateChangedResponse updateState(Agent agent, AgentState newState, boolean isChangedInternally) {
        Enums.AgentStateName currentState = agent.getState().getName();

        if (currentState.equals(Enums.AgentStateName.NOT_READY)) {
            agent.setState(newState);
            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            return new AgentStateChangedResponse(null, true, new ArrayList<>());
        }

        if (currentState.equals(Enums.AgentStateName.READY)) {
            if (agent.getReservedTask() != null) {
                String exceptThisMrd = agent.getReservedTask().getMrd().getId();
                List<String> mrdStateChanges = this.updateAgentMrdStates(agent, exceptThisMrd, isChangedInternally);
                return new AgentStateChangedResponse(null, false, mrdStateChanges);
            }

            List<String> mrdStateChanges = this.updateAgentMrdStates(agent, null, isChangedInternally);

            if (isAnyMrdInAvailableState(agent)) {
                return new AgentStateChangedResponse(null, false, mrdStateChanges);
            }

            agent.setState(newState);
            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            return new AgentStateChangedResponse(null, true, mrdStateChanges);
        }

        return new AgentStateChangedResponse(null, false, new ArrayList<>());
    }

    boolean isAnyMrdInAvailableState(Agent agent) {
        List<AgentMrdState> agentMrdStates = agent.getAgentMrdStates();
        for (AgentMrdState agentMrdState : agentMrdStates) {
            if (!(agentMrdState.getState().equals(Enums.AgentMrdStateName.NOT_READY)
                    || agentMrdState.getState().equals(Enums.AgentMrdStateName.PENDING_NOT_READY))) {
                return true;
            }
        }
        return false;
    }

    List<String> updateAgentMrdStates(Agent agent, String except, boolean isChangedInternally) {
        List<AgentMrdState> agentMrdStates = agent.getAgentMrdStates();
        List<String> mrdStateChanges = new ArrayList<>();

        for (AgentMrdState agentMrdState : agentMrdStates) {
            String mrdId = agentMrdState.getMrd().getId();
            MrdType mrdType = this.mrdTypePool.getById(agentMrdState.getMrd().getType());

            if (mrdId.equals(except) || (isChangedInternally && !mrdType.isManagedByRe())) {
                continue;
            }

            if (agent.getNoOfActiveQueueTasks(mrdId) > 0
                    && !agentMrdState.getState().equals(Enums.AgentMrdStateName.PENDING_NOT_READY)) {
                agentMrdState.setState(Enums.AgentMrdStateName.PENDING_NOT_READY);
                mrdStateChanges.add(mrdId);
            } else if (!agentMrdState.getState().equals(Enums.AgentMrdStateName.NOT_READY)) {
                agentMrdState.setState(Enums.AgentMrdStateName.NOT_READY);
                mrdStateChanges.add(mrdId);
            }
        }
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
        return mrdStateChanges;
    }
}
