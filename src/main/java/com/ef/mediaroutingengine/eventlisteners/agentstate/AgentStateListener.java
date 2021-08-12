package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Agent state listener.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateListener {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStateListener.class);
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Factory.
     */
    private final AgentStateDelegateFactory factory;

    /**
     * Constructor. Load the required beans
     *
     * @param agentPresenceRepository agent presence repository DAO
     * @param jmsCommunicator         the jms communicator
     * @param factory                 the factory
     */
    @Autowired
    public AgentStateListener(AgentPresenceRepository agentPresenceRepository,
                              JmsCommunicator jmsCommunicator, AgentStateDelegateFactory factory) {
        this.agentPresenceRepository = agentPresenceRepository;
        this.jmsCommunicator = jmsCommunicator;
        this.factory = factory;
    }


    /**
     * Property change.
     *
     * @param agent    the agent
     * @param newState the new state
     */
    public void propertyChange(Agent agent, AgentState newState) {
        LOGGER.debug("Method started");
        CompletableFuture.runAsync(() -> this.asyncPropertyChange(agent, newState));
        LOGGER.debug("Method ended");
    }


    /**
     * Async property change.
     *
     * @param agent    the agent
     * @param newState the new state
     */
    private void asyncPropertyChange(Agent agent, AgentState newState) {
        LOGGER.info("Property change event: Agent-State called");
        LOGGER.info("Current state: {}, New state: {}", agent.getState().getName(), newState);

        AgentStateDelegate delegate = factory.getDelegate(newState.getName());
        if (delegate == null) {
            return;
        }
        boolean isStateChanged = delegate.updateState(agent, newState);
        LOGGER.info("Before Publishing state change on JMS");
        this.publish(this.agentPresenceRepository.find(agent.getId().toString()), isStateChanged);
        LOGGER.info("Agent state change request published on JMS");
    }

    /**
     * Publish.
     *
     * @param agentPresence     the agent presence
     * @param agentStateChanged the agent state changed
     */
    private void publish(AgentPresence agentPresence, boolean agentStateChanged) {
        AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, agentStateChanged);
        try {
            jmsCommunicator.publish(res, Enums.JmsEventName.AGENT_STATE_CHANGED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
