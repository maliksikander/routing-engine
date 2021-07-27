package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStateLogin.class);
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
        AgentPresence agentPresence = this.addToAddPresenceDbIfDoesNotExist(agent);
        if (currentState.equals(Enums.AgentStateName.LOGOUT)) {
            this.logoutToLogin(agent, agentPresence, newState);
            LOGGER.debug("State changed from LOGOUT to LOGIN");
            loginToNotReady(agent, new AgentState(Enums.AgentStateName.NOT_READY, newState.getReasonCode()));
            LOGGER.debug("State changed from LOGIN to NOT_READY");
            return true;
        }
        return false;
    }

    /**
     * Logout to login.
     *
     * @param agent         the agent
     * @param agentPresence the agent presence
     * @param state         the state
     */
    private void logoutToLogin(Agent agent, AgentPresence agentPresence, AgentState state) {
        agent.setState(state);
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            agentMrdStates.add(new AgentMrdState(agentMrdState.getMrd(), Enums.AgentMrdStateName.LOGIN));
        }
        agent.setAgentMrdStates(agentMrdStates);
        agentPresence.setAgent(agent.toCcUser());
        agentPresence.setStateChangeTime(new Timestamp(System.currentTimeMillis()));
        agentPresence.setState(agent.getState());
        agentPresence.setAgentMrdStates(agentMrdStates);
        this.publish(agentPresence);
    }

    /**
     * Login to not ready.
     *
     * @param agent the agent
     * @param state the state
     */
    private void loginToNotReady(Agent agent, AgentState state) {
        agent.setState(state);
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            agentMrdStates.add(new AgentMrdState(agentMrdState.getMrd(), Enums.AgentMrdStateName.NOT_READY));
        }
        agent.setAgentMrdStates(agentMrdStates);
        this.agentPresenceRepository.updateAgentState(agent.getId(), agent.getState());
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agentMrdStates);
    }

    /**
     * Add to add presence db if does not exist agent presence.
     *
     * @param agent the agent
     * @return the agent presence
     */
    private AgentPresence addToAddPresenceDbIfDoesNotExist(Agent agent) {
        AgentPresence agentPresence = agentPresenceRepository.find(agent.getId().toString());
        if (agentPresence == null) {
            agentPresence = new AgentPresence(agent.toCcUser(), agent.getState(), agent.getAgentMrdStates());
            this.agentPresenceRepository.save(agent.getId().toString(), agentPresence);
        }
        return agentPresence;
    }

    /**
     * Publish.
     *
     * @param agentPresence the agent presence
     */
    private void publish(AgentPresence agentPresence) {
        AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, true);
        try {
            jmsCommunicator.publish(res, Enums.JmsEventName.AGENT_STATE_CHANGED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
