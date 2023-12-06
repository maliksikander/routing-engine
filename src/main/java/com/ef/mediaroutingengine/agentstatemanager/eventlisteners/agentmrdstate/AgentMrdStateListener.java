package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskType;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.TaskUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Agent presence repository DAO.
     */
    private final AgentPresenceRepository agentPresenceRepository;
    /**
     * The Jms communicator to publish agent presence on topic.
     */
    private final JmsCommunicator jmsCommunicator;
    private final MrdPool mrdPool;
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
                                 JmsCommunicator jmsCommunicator, MrdPool mrdPool,
                                 MrdStateDelegateFactory factory) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.jmsCommunicator = jmsCommunicator;
        this.mrdPool = mrdPool;
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
            String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
            CompletableFuture.runAsync(() -> {
                // putting same correlation id from the caller thread into this thread.
                MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
                this.run(agent, mrdId, requestedState);
                MDC.clear();
            });
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
    void run(Agent agent, String mrdId, Enums.AgentMrdStateName requestedState) {
        logger.info("MRD state change requested to {} | MRD: {}, Agent: {}", requestedState, mrdId, agent.getId());
        AgentMrdState agentMrdState = agent.getAgentMrdState(mrdId);
        if (agentMrdState == null) {
            logger.error("Could not find MRD with id: {} associated with agent: {}", mrdId, agent.getId());
            this.publish(agent, Enums.JmsEventName.AGENT_STATE_UNCHANGED, new ArrayList<>());
            return;
        }

        MrdStateDelegate delegate = this.factory.getDelegate(requestedState);
        if (delegate == null) {
            logger.warn("Requested Agent-MRD state: {} is invalid, ignoring request..", requestedState);
            return;
        }
        synchronized (agentMrdState) {
            Enums.AgentMrdStateName currentState = agentMrdState.getState();
            Enums.AgentMrdStateName newState = delegate.getNewState(agent, agentMrdState);

            logger.debug("new state after evaluating states is {}", newState);

            if (!newState.equals(currentState)) {
                this.updateState(agent, agentMrdState, newState);
                logger.info("MRD state changed from: {} to: {} | MRD: {} | Agent: {}", currentState, newState,
                        mrdId, agent.getId());

                List<String> mrdStateChanges = new ArrayList<>();
                mrdStateChanges.add(mrdId);

                this.publish(agent, Enums.JmsEventName.AGENT_STATE_CHANGED, mrdStateChanges);

                if (isStateReadyOrActive(newState)) {
                    logger.debug("Triggering task-routers for MRD: {}", agentMrdState.getMrd().getId());
                    this.precisionQueuesPool.publishAgentAvailable(agentMrdState);
                }
            } else {
                logger.info("MRD state change from: {} to: {} not allowed | MRD: {} | Agent: {}", currentState,
                        newState, mrdId, agent.getId());
                this.publish(agent, Enums.JmsEventName.AGENT_STATE_UNCHANGED, new ArrayList<>());
            }
        }
    }

    boolean isStateReadyOrActive(Enums.AgentMrdStateName newState) {
        return newState.equals(Enums.AgentMrdStateName.READY) || newState.equals(Enums.AgentMrdStateName.ACTIVE);
    }

    /**
     * Update Agent-MRD state in in-memory object as well as in Redis collection.
     *
     * @param agent         the agent
     * @param agentMrdState the agent mrd state
     * @param state         the state
     */
    void updateState(Agent agent, AgentMrdState agentMrdState, Enums.AgentMrdStateName state) {
        agentMrdState.setState(state);
        logger.debug("agent {} MRDs state after updating in memory agent object {} ",
                agent.getKeycloakUser().getUsername(),
                agent.getAgentMrdStates());

        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
    }

    /**
     * Publishes updated Agent-Presence on JMS-topic.
     *
     * @param agent the agent
     */
    void publish(Agent agent, Enums.JmsEventName eventName, List<String> mrdStateChanges) {
        try {
            AgentPresence agentPresence = this.agentPresenceRepository.find(agent.getId());
            AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, false, mrdStateChanges);
            jmsCommunicator.publish(res, eventName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Change state on media close.
     *
     * @param agent the agent
     * @param media the media
     */
    public void changeStateOnMediaClose(Agent agent, TaskMedia media) {
        String mrdId = media.getMrdId();
        MrdType mrdType = this.mrdPool.getType(mrdId);

        TaskType type = media.getType();

        if (!(type.getMode().equals(Enums.TaskTypeMode.QUEUE) || TaskUtility.isNamedAgentTransfer(type))
                || !mrdType.isManagedByRe()) {
            return;
        }

        Enums.AgentMrdStateName currentMrdState = agent.getAgentMrdState(mrdId).getState();
        int noOfTasks = agent.getNoOfActiveQueueTasks(mrdId);
        int maxAgentTasks = agent.getAgentMrdState(mrdId).getMaxAgentTasks();

        if (currentMrdState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY) && noOfTasks < 1) {
            this.propertyChange(agent, mrdId, Enums.AgentMrdStateName.NOT_READY, true);
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.BUSY)) {
            if (noOfTasks < 1) {
                this.propertyChange(agent, mrdId, Enums.AgentMrdStateName.READY, true);
            } else if (noOfTasks < maxAgentTasks) {
                this.propertyChange(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, true);
            }
        } else if (currentMrdState.equals(Enums.AgentMrdStateName.ACTIVE)) {
            if (noOfTasks >= maxAgentTasks) {
                this.propertyChange(agent, mrdId, Enums.AgentMrdStateName.BUSY, true);
            } else if (noOfTasks < 1) {
                this.propertyChange(agent, mrdId, Enums.AgentMrdStateName.READY, true);
            }
        }
    }

    /**
     * Change state on media active.
     *
     * @param agent the agent
     * @param media the media
     */
    public void changeStateOnMediaActive(Agent agent, TaskMedia media) {
        logger.debug("method started");

        String mrdId = media.getMrdId();
        MrdType mrdType = this.mrdPool.getType(mrdId);

        logger.debug("MrdType: {}", mrdType);

        if (!mrdType.isManagedByRe()) {
            return;
        }

        int noOfTasks = agent.getNoOfActiveQueueTasks(mrdId);
        int maxAllowedTasks = agent.getAgentMrdState(mrdId).getMaxAgentTasks();

        logger.debug("noOfTasks: {}, maxAllowedTasks: {}", noOfTasks, maxAllowedTasks);

        if (noOfTasks >= maxAllowedTasks) {
            this.propertyChange(agent, mrdId, Enums.AgentMrdStateName.BUSY, false);
        } else if (noOfTasks == 1) {
            this.propertyChange(agent, mrdId, Enums.AgentMrdStateName.ACTIVE, false);
        }

        logger.debug("method ended");
    }
}
