package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.dto.AgentMrdStateChangedRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentStateListener implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStateListener.class);
    private final AgentsPool agentsPool;
    private final AgentPresenceRepository agentPresenceRepository;
    private final PropertyChangeSupport propertyChangeSupport;

    /**
     * Constructor. Load the required beans
     * @param agentsPool pool of all agents
     * @param agentPresenceRepository agent presence repository DAO
     * @param agentMrdStateEvent listens to changes in Agent's MRD states
     */
    @Autowired
    public AgentStateListener(AgentsPool agentsPool, AgentPresenceRepository agentPresenceRepository,
                              AgentMrdStateEvent agentMrdStateEvent) {
        this.agentsPool = agentsPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.propertyChangeSupport.addPropertyChangeListener(Enums.EventName.AGENT_MRD_STATE.name(),
                agentMrdStateEvent);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.AGENT_STATE.name())) {
            AgentStateChangeRequest request = (AgentStateChangeRequest) evt.getNewValue();
            Agent agent = this.agentsPool.findById(request.getAgentId());
            if (agent == null) {
                LOGGER.info("Could not find Agent with id: {} in the agents pool", request.getAgentId());
                return;
            }

            Enums.AgentStateName currentState = agent.getState();
            Enums.AgentStateName newState = request.getState();

            switch (newState) {
                case LOGIN:
                    AgentPresence agentPresence = agentPresenceRepository.find(agent.getId().toString());
                    if (agentPresence == null) {
                        // Add to agent presence.
                    }

                    if (currentState.equals(Enums.AgentStateName.LOGOUT)) {
                        agent.setState(Enums.AgentStateName.LOGIN);

                        AgentMrdStateChangedRequest mrdStateChangedRequest = new AgentMrdStateChangedRequest();
                        mrdStateChangedRequest.setAgentId(agent.getId());
                        mrdStateChangedRequest.setAgentMrdStates(agent.getAgentMrdStates());
                    }
                    break;
                case NOT_READY:
                    if (currentState.equals(Enums.AgentStateName.LOGIN)) {
                        agent.setState(newState);
                    } else if (currentState.equals(Enums.AgentStateName.READY)) {
                        // Once an agent switches the state to NOT_READY all MRD switches will be turned off.
                    }
                    break;
                case READY:
                    if (currentState.equals(Enums.AgentStateName.NOT_READY)) {
                        agent.setState(newState);
                    }
                    break;
                case LOGOUT:
                    if (currentState.equals(Enums.AgentStateName.NOT_READY)) {
                        // Switch off all MRDs
                        // Re-route all tasks
                        // Publish Logout Event on topic
                    } else {
                        // Re-Route Any active tasks.
                        // Publish Logout Event.
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
