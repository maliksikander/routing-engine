package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Agent state login.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateLogin implements AgentStateDelegate {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(AgentStateLogin.class);
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Agent state login.
     *
     * @param agentPresenceRepository the agent presence repository
     * @param jmsCommunicator         the jms communicator
     */
    @Autowired
    public AgentStateLogin(AgentPresenceRepository agentPresenceRepository,
                           JmsCommunicator jmsCommunicator) {
        this.agentPresenceRepository = agentPresenceRepository;
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public boolean updateState(Agent agent, AgentState newState) {
        Enums.AgentStateName currentState = agent.getState().getName();
        if (currentState.equals(Enums.AgentStateName.LOGOUT)) {
            this.logoutToLogin(agent, newState);
            logger.info("Agent-state changed from LOGOUT to LOGIN | Agent: {}", agent.getId());
            loginToNotReady(agent, new AgentState(Enums.AgentStateName.NOT_READY, newState.getReasonCode()));
            return true;
        }
        return false;
    }

    /**
     * Logout to login.
     *
     * @param agent the agent
     * @param state the state
     */
    void logoutToLogin(Agent agent, AgentState state) {
        agent.setState(state);
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            agentMrdState.setState(Enums.AgentMrdStateName.LOGIN);
        }
        AgentPresence agentPresence = this.agentPresenceRepository.find(agent.getId());
        agentPresence.setState(agent.getState());
        agentPresence.setAgentMrdStates(agent.getAgentMrdStates());
        this.publish(agentPresence);
    }

    /**
     * Login to not ready.
     *
     * @param agent the agent
     * @param state the state
     */
    void loginToNotReady(Agent agent, AgentState state) {
        agent.setState(state);
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            agentMrdState.setState(Enums.AgentMrdStateName.NOT_READY);
        }
        this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
    }

    /**
     * Publish.
     *
     * @param agentPresence the agent presence
     */
    void publish(AgentPresence agentPresence) {
        AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, true);
        try {
            jmsCommunicator.publish(res, Enums.JmsEventName.AGENT_STATE_CHANGED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
