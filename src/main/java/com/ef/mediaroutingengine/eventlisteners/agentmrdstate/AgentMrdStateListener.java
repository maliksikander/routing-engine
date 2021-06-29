package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentMrdStateListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentMrdStateListener.class);

    private final AgentsPool agentsPool;
    private final List<PrecisionQueue> precisionQueues;
    private final AgentPresenceRepository agentPresenceRepository;
    private final JmsCommunicator jmsCommunicator;
    private final MrdStateDelegateFactory factory;

    /**
     * Default Constructor.
     *
     * @param agentsPool              pool of all agents
     * @param precisionQueuesPool     pool of all precision queues
     * @param agentPresenceRepository Agent-presence repository DAO
     * @param jmsCommunicator         Publishes state-change events on the JMS topic
     * @param factory                 Mrd Delegate factory gives the appropriate Delegate to get the new State
     */
    @Autowired
    public AgentMrdStateListener(AgentsPool agentsPool, PrecisionQueuesPool precisionQueuesPool,
                                 AgentPresenceRepository agentPresenceRepository,
                                 JmsCommunicator jmsCommunicator,
                                 MrdStateDelegateFactory factory) {
        this.agentsPool = agentsPool;
        this.precisionQueues = precisionQueuesPool.toList();
        this.agentPresenceRepository = agentPresenceRepository;
        this.jmsCommunicator = jmsCommunicator;
        this.factory = factory;
    }

    /**
     * This is method called by a event publisher if Agent MRD state needs to be changed.
     *
     * @param evt   event object contains the event name and value.
     * @param async if true the event is handled asynchronously. If false event is handled synchronously.
     */
    public void propertyChange(PropertyChangeEvent evt, boolean async) {
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.AGENT_MRD_STATE.name())) {
            if (async) {
                CompletableFuture.runAsync(() -> this.asyncPropertyChange(evt));
            } else {
                this.asyncPropertyChange(evt);
            }
        }
    }

    private void asyncPropertyChange(PropertyChangeEvent evt) {
        AgentMrdStateChangeRequest request = (AgentMrdStateChangeRequest) evt.getNewValue();
        Agent agent = this.agentsPool.findById(request.getAgentId());

        if (agent == null) {
            LOGGER.error("Could not find Agent with id: {} in the agents pool", request.getAgentId());
            return;
        }

        AgentMrdState agentMrdState = agent.getAgentMrdState(request.getMrdId());
        if (agentMrdState == null) {
            LOGGER.error("Could not find MRD with id: {} associated with agent: {}", request.getMrdId(),
                    request.getAgentId());
            return;
        }

        MrdStateDelegate delegate = this.factory.getDelegate(request.getState());
        if (delegate == null) {
            LOGGER.warn("Incorrect Agent mrd state requested: {}", request.getState());
            return;
        }

        Enums.AgentMrdStateName currentState = agentMrdState.getState();
        Enums.AgentMrdStateName newState = delegate.getNewState(agent, agentMrdState);
        if (!newState.equals(currentState)) {
            this.updateState(agent, agentMrdState, newState);
            if (newState.equals(Enums.AgentMrdStateName.READY)
                    || newState.equals(Enums.AgentMrdStateName.ACTIVE)) {
                this.fireStateChangeToTaskSchedulers(agentMrdState);
            }
        }
        this.publish(agent);
        LOGGER.debug("Agent State Listener for agent: {}", agent.getId());
    }

    private void updateState(Agent agent, AgentMrdState agentMrdState, Enums.AgentMrdStateName state) {
        agentMrdState.setState(state);
        this.agentPresenceRepository.updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
    }

    private void publish(Agent agent) {
        try {
            AgentPresence agentPresence = this.agentPresenceRepository.find(agent.getId().toString());
            AgentStateChangedResponse res = new AgentStateChangedResponse(agentPresence, false);
            jmsCommunicator.publish(res, Enums.RedisEventName.AGENT_STATE_CHANGED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
