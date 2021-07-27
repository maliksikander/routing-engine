package com.ef.mediaroutingengine.eventlisteners.agentstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
public class AgentStateListener implements PropertyChangeListener {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStateListener.class);
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
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
     * @param agentsPool              pool of all agents
     * @param agentPresenceRepository agent presence repository DAO
     * @param jmsCommunicator         the jms communicator
     * @param factory                 the factory
     */
    @Autowired
    public AgentStateListener(AgentsPool agentsPool, AgentPresenceRepository agentPresenceRepository,
                              JmsCommunicator jmsCommunicator, AgentStateDelegateFactory factory) {
        this.agentsPool = agentsPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.jmsCommunicator = jmsCommunicator;
        this.factory = factory;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LOGGER.debug("Method started");
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.AGENT_STATE.name())) {
            CompletableFuture.runAsync(() -> this.asyncPropertyChange(evt));
        }
        LOGGER.debug("Method ended");
    }

    /**
     * Async property change.
     *
     * @param evt the evt
     */
    private void asyncPropertyChange(PropertyChangeEvent evt) {
        LOGGER.info("Property change event: {} called", evt.getPropertyName());
        AgentStateChangeRequest request = (AgentStateChangeRequest) evt.getNewValue();
        Agent agent = this.agentsPool.findById(request.getAgentId());
        if (agent == null) {
            LOGGER.error("Could not find Agent with id: {} in the agents pool", request.getAgentId());
            return;
        }
        LOGGER.info("Current state: {}, New state: {}", agent.getState().getName(), request.getState());

        AgentStateDelegate delegate = factory.getDelegate(request.getState().getName());
        if (delegate == null) {
            return;
        }
        boolean stateChanged = delegate.updateState(agent, request.getState());
        LOGGER.info("Before Publishing state change on JMS");
        this.publish(this.agentPresenceRepository.find(agent.getId().toString()), stateChanged);
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
