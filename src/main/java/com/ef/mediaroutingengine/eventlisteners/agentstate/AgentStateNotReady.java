package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateNotReady implements AgentStateDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStateNotReady.class);
    private final AgentPresenceRepository agentPresenceRepository;

    public AgentStateNotReady(AgentPresenceRepository agentPresenceRepository) {
        this.agentPresenceRepository = agentPresenceRepository;
    }

    @Override
    public boolean updateState(Agent agent, AgentState newState) {
        Enums.AgentStateName currentState = agent.getState().getName();
        if (currentState.equals(Enums.AgentStateName.READY)) {
            agent.setState(newState);
            List<AgentMrdState> agentMrdStates = agent.getAgentMrdStates();
            for (AgentMrdState agentMrdState : agentMrdStates) {
                UUID mrdId = agentMrdState.getMrd().getId();
                if (agent.getNoOfActiveTasks(mrdId) > 0) {
                    agentMrdState.setState(Enums.AgentMrdStateName.PENDING_NOT_READY);
                } else {
                    agentMrdState.setState(Enums.AgentMrdStateName.NOT_READY);
                }
            }
            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agentMrdStates);
            return true;
        } else if (currentState.equals(Enums.AgentStateName.NOT_READY)) {
            agent.setState(newState);
            this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
            return true;
        }
        return false;
    }
}
