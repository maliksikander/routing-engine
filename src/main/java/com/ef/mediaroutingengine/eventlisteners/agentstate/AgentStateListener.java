package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private static final Logger logger = LoggerFactory.getLogger(AgentStateListener.class);
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
        logger.debug("Agent state listener called asynchronously");
        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
        CompletableFuture.runAsync(() -> {
            // putting same correlation id from the caller thread into this thread
            MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
            this.run(agent, newState);
            MDC.clear();
        });
    }


    /**
     * Async property change.
     *
     * @param agent    the agent
     * @param newState the new state
     */
    public void run(Agent agent, AgentState newState) {
        logger.info("Agent state change requested | Agent: {}", agent.getId());

        AgentStateDelegate delegate = factory.getDelegate(newState.getName());
        if (delegate == null) {
            logger.warn("Requested Agent state: {} is invalid, ignoring request..", newState);
            return;
        }

        AgentState currentState = agent.getState();
        boolean isStateChanged = delegate.updateState(agent, newState);

        if (isStateChanged) {
            logger.info("Agent state changed from {} to {} | Agent: {}", currentState, newState, agent.getId());
            this.publish(agent, Enums.JmsEventName.AGENT_STATE_CHANGED, true);
        } else {
            logger.info("Agent-state change from: {} to: {} not allowed | Agent: {}",
                    currentState, newState, agent.getId());
            this.publish(agent, Enums.JmsEventName.AGENT_STATE_UNCHANGED, false);
        }
    }

    /**
     * Publish.
     *
     * @param agent     the agent presence
     * @param agentStateChanged the agent state changed
     */
    private void publish(Agent agent, Enums.JmsEventName eventName, boolean agentStateChanged) {
        AgentPresence agentPresence = this.agentPresenceRepository.find(agent.getId().toString());
        AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, agentStateChanged);
        try {
            jmsCommunicator.publish(res, eventName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
