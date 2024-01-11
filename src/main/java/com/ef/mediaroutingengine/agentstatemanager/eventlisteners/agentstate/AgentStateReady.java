package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.model.Agent;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Agent state ready.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateReady implements AgentStateDelegate {
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;

    /**
     * Instantiates a new Agent state ready.
     *
     * @param agentPresenceRepository the agent presence repository
     */
    @Autowired
    public AgentStateReady(AgentPresenceRepository agentPresenceRepository) {
        this.agentPresenceRepository = agentPresenceRepository;
    }

    @Override
    public AgentStateChangedResponse updateState(Agent agent, AgentState newState, boolean isChangedInternally) {
        Enums.AgentStateName currentState = agent.getState().getName();
        if (currentState.equals(Enums.AgentStateName.NOT_READY)) {
            agent.setState(newState);
            this.agentPresenceRepository.updateAgentState(agent.getId(), newState);
            return new AgentStateChangedResponse(null, true, new ArrayList<>());
        }
        return new AgentStateChangedResponse(null, false, new ArrayList<>());
    }
}
