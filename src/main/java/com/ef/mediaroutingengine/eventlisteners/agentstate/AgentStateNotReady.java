package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
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

    /**
     * Instantiates a new Agent state not ready.
     *
     * @param agentPresenceRepository the agent presence repository
     */
    public AgentStateNotReady(AgentPresenceRepository agentPresenceRepository) {
        this.agentPresenceRepository = agentPresenceRepository;
    }

    @Override
    public boolean updateState(Agent agent, AgentState newState) {
        Enums.AgentStateName currentState = agent.getState().getName();

        if (currentState.equals(Enums.AgentStateName.NOT_READY)) {
            agent.setState(newState);
            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            return true;
        }

        if (currentState.equals(Enums.AgentStateName.READY)) {
            List<String> exceptTheseMrd = this.getMrdWithReservedTasks(agent);

            agent.setState(newState);
            this.updateAgentMrdStates(agent, exceptTheseMrd);

            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());

            return true;
        }

        return false;
    }

    void updateAgentMrdStates(Agent agent, List<String> except) {
        List<AgentMrdState> agentMrdStates = agent.getAgentMrdStates();
        for (AgentMrdState agentMrdState : agentMrdStates) {
            String mrdId = agentMrdState.getMrd().getId();

            if (isMrdInExceptionList(mrdId, except)) {
                continue;
            }

            if (agent.getNoOfActivePushTasks(mrdId) > 0) {
                agentMrdState.setState(Enums.AgentMrdStateName.PENDING_NOT_READY);
            } else {
                agentMrdState.setState(Enums.AgentMrdStateName.NOT_READY);
            }
        }
    }

    List<String> getMrdWithReservedTasks(Agent agent) {
        List<String> mrdListWithReservedTasks = new ArrayList<>();

        if (agent.getReservedTask() != null && agent.getVoiceReservedTask() != null) {
            mrdListWithReservedTasks.add(agent.getReservedTask().getMrd().getId());
            mrdListWithReservedTasks.add(agent.getVoiceReservedTask().getMrd().getId());
        } else if (agent.getReservedTask() != null) {
            mrdListWithReservedTasks.add(agent.getReservedTask().getMrd().getId());
        } else if (agent.getVoiceReservedTask() != null) {
            mrdListWithReservedTasks.add(agent.getVoiceReservedTask().getMrd().getId());
        }

        return mrdListWithReservedTasks;

    }

    private boolean isMrdInExceptionList(String mrdId, List<String> exceptionList) {
        for (String exception : exceptionList) {
            if (mrdId.equals(exception)) {
                return true;
            }
        }

        return false;
    }
}
