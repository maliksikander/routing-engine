package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The type Agent mrd state listener.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentMrdStateListener {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(AgentMrdStateListener.class);
    /**
     * In-memory pool of all Precision queues.
     */
    private final List<PrecisionQueue> precisionQueues;
    /**
     * The Agent presence repository DAO.
     */
    private final AgentPresenceRepository agentPresenceRepository;
    /**
     * The Jms communicator to publish agent presence on topic.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * Creates and return the appropriate state change delegate.
     */
    private final MrdStateDelegateFactory factory;

    /**
     * Default Constructor.
     *
     * @param precisionQueuesPool     pool of all precision queues
     * @param agentPresenceRepository Agent-presence repository DAO
     * @param jmsCommunicator         Publishes state-change events on the JMS topic
     * @param factory                 Mrd Delegate factory gives the appropriate Delegate to get the new State
     */
    @Autowired
    public AgentMrdStateListener(PrecisionQueuesPool precisionQueuesPool,
                                 AgentPresenceRepository agentPresenceRepository,
                                 JmsCommunicator jmsCommunicator,
                                 MrdStateDelegateFactory factory) {
        this.precisionQueues = precisionQueuesPool.toList();
        this.agentPresenceRepository = agentPresenceRepository;
        this.jmsCommunicator = jmsCommunicator;
        this.factory = factory;
    }

    /**
     * Property change.
     *
     * @param agent          the agent
     * @param mrdId          the mrd id
     * @param requestedState the requested state
     * @param async          the async
     */
    public void propertyChange(Agent agent, String mrdId, Enums.AgentMrdStateName requestedState, boolean async) {
        if (async) {
            logger.debug("Agent mrd state listener called asynchronously");
            CompletableFuture.runAsync(() -> this.run(agent, mrdId, requestedState));
        } else {
            logger.debug("Agent mrd state listener called synchronously");
            this.run(agent, mrdId, requestedState);
        }
    }

    /**
     * Async property change.
     *
     * @param agent          the agent
     * @param mrdId          the mrd id
     * @param requestedState the requested state
     */
    private void run(Agent agent, String mrdId, Enums.AgentMrdStateName requestedState) {
        AgentMrdState agentMrdState = agent.getAgentMrdState(mrdId);
        if (agentMrdState == null) {
            logger.error("Could not find MRD with id: {} associated with agent: {}", mrdId, agent.getId());
            this.publish(agent);
            return;
        }

        MrdStateDelegate delegate = this.factory.getDelegate(requestedState);
        if (delegate == null) {
            logger.warn("Requested Agent-MRD state: {} is invalid", requestedState);
            return;
        }

        Enums.AgentMrdStateName currentState = agentMrdState.getState();
        Enums.AgentMrdStateName newState = delegate.getNewState(agent, agentMrdState);
        boolean fireEvent = false;
        if (!newState.equals(currentState)) {
            this.updateState(agent, agentMrdState, newState);
            logger.debug("Agent-Mrd state for agent: {} updated to: {} from: {}", agent.getId(), newState,
                    currentState);
            if (newState.equals(Enums.AgentMrdStateName.READY)
                    || newState.equals(Enums.AgentMrdStateName.ACTIVE)) {
                fireEvent = true;
            }
        }
        this.publish(agent);
        logger.debug("Updated AgentPresence for agent: {} published on topic", agent.getId());
        if (fireEvent) {
            logger.debug("Task Schedulers for MRD: {} triggerred by agent mrd state change to: {}",
                    agentMrdState.getMrd().getId(), newState);
            this.fireStateChangeToTaskSchedulers(agentMrdState);
        }
    }

    /**
     * Update Agent-MRD state in in-memory object as well as in Redis collection.
     *
     * @param agent         the agent
     * @param agentMrdState the agent mrd state
     * @param state         the state
     */
    private void updateState(Agent agent, AgentMrdState agentMrdState, Enums.AgentMrdStateName state) {
        agentMrdState.setState(state);
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
    }

    /**
     * Publishes updated Agent-Presence on JMS-topic.
     *
     * @param agent the agent
     */
    private void publish(Agent agent) {
        try {
            AgentPresence agentPresence = this.agentPresenceRepository.find(agent.getId().toString());
            AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, false);
            jmsCommunicator.publish(res, Enums.JmsEventName.AGENT_STATE_CHANGED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fire state change to task schedulers.
     *
     * @param agentMrdState the agent mrd state
     */
    private void fireStateChangeToTaskSchedulers(AgentMrdState agentMrdState) {
        String eventName = "AGENT_MRD_STATE_" + agentMrdState.getState().name();
        for (PrecisionQueue precisionQueue : this.precisionQueues) {
            if (precisionQueue.getMrd().getId().equals(agentMrdState.getMrd().getId())) {
                PropertyChangeEvent evt = new PropertyChangeEvent(this, eventName, null, agentMrdState);
                precisionQueue.getTaskScheduler().propertyChange(evt);
            }
        }
    }
}
