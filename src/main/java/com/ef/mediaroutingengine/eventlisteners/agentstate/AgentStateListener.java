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

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateListener implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStateListener.class);
    private final AgentsPool agentsPool;
    private final AgentPresenceRepository agentPresenceRepository;
    private final JmsCommunicator jmsCommunicator;
    private final AgentStateDelegateFactory factory;

    /**
     * Constructor. Load the required beans
     *
     * @param agentsPool              pool of all agents
     * @param agentPresenceRepository agent presence repository DAO
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

    private void publish(AgentPresence agentPresence, boolean agentStateChanged) {
        AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, agentStateChanged);
        try {
            jmsCommunicator.publish(res, Enums.RedisEventName.AGENT_STATE_CHANGED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
