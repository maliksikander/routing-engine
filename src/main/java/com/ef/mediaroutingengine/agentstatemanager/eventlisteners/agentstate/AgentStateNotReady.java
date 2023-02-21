package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.model.Agent;
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

    /**
     * Instantiates a new Agent state not ready.
     *
     * @param agentPresenceRepository the agent presence repository
     */
    public AgentStateNotReady(AgentPresenceRepository agentPresenceRepository) {
        this.agentPresenceRepository = agentPresenceRepository;
    }

    @Override
    public boolean updateState(Agent agent, AgentState newState, boolean isChangedInternally) {
        Enums.AgentStateName currentState = agent.getState().getName();

        if (currentState.equals(Enums.AgentStateName.NOT_READY)) {
            agent.setState(newState);
            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            return true;
        }

        if (currentState.equals(Enums.AgentStateName.READY)) {
            if (agent.getReservedTask() != null) {
                String exceptThisMrd = agent.getReservedTask().getMrd().getId();
                this.updateAgentMrdStates(agent, exceptThisMrd, isChangedInternally);
                this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
                return false;
            }

            this.updateAgentMrdStates(agent, null, isChangedInternally);

            if (isAnyMrdInAvailableState(agent)) {
                return false;
            }

            agent.setState(newState);
            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            return true;
        }

        return false;
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

    void updateAgentMrdStates(Agent agent, String except, boolean isChangedInternally) {
        List<AgentMrdState> agentMrdStates = agent.getAgentMrdStates();
        for (AgentMrdState agentMrdState : agentMrdStates) {
            String mrdId = agentMrdState.getMrd().getId();

            if (mrdId.equals(except) || (isChangedInternally && !agentMrdState.getMrd().isManagedByRe())) {
                continue;
            }
            if (agent.getNoOfActiveQueueTasks(mrdId) > 0) {
                agentMrdState.setState(Enums.AgentMrdStateName.PENDING_NOT_READY);
            } else {
                agentMrdState.setState(Enums.AgentMrdStateName.NOT_READY);
            }

        }
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
    }

}
