package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.AssociatedMrd;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Agent state service.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateService {
    /**
     * The Agent state listener.
     */
    private final AgentStateListener agentStateListener;
    /**
     * The Agent mrd state listener.
     */
    private final AgentMrdStateListener agentMrdStateListener;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;
    /**
     * The Agents repository.
     */
    private final AgentsRepository agentsRepository;

    /**
     * Instantiates a new Agent state service.
     *
     * @param agentStateListener      the agent state listener
     * @param agentMrdStateListener   the agent mrd state listener
     * @param agentsPool              the agents pool
     * @param mrdPool                 the mrd pool
     * @param agentPresenceRepository the agent presence repository
     * @param agentsRepository        the agents repository
     */
    @Autowired
    public AgentStateService(AgentStateListener agentStateListener, AgentMrdStateListener agentMrdStateListener,
                             AgentsPool agentsPool, MrdPool mrdPool,
                             AgentPresenceRepository agentPresenceRepository,
                             AgentsRepository agentsRepository) {
        this.agentStateListener = agentStateListener;
        this.agentMrdStateListener = agentMrdStateListener;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.agentsRepository = agentsRepository;
    }

    /**
     * Handles the agent login request.
     *
     * @param request AgentLoginRequest DTO.
     */
    public void agentLogin(KeycloakUser request) {
        Agent agent = this.agentsPool.findById(request.getId());
        if (agent == null) {
            CCUser ccUser = this.getCcUserInstance(request);
            agent = new Agent(ccUser, mrdPool.findAll());
            AgentPresence agentPresence = new AgentPresence(ccUser, agent.getState(), agent.getAgentMrdStates());

            this.agentsRepository.save(ccUser);
            this.agentPresenceRepository.save(agent.getId().toString(), agentPresence);
            this.agentsPool.insert(agent);
        }

        this.agentStateListener.propertyChange(agent, new AgentState(Enums.AgentStateName.LOGIN, null));
    }

    /**
     * Gets cc user instance.
     *
     * @param keycloakUser the keycloak user
     * @return the cc user instance
     */
    private CCUser getCcUserInstance(KeycloakUser keycloakUser) {
        CCUser ccUser = new CCUser();
        ccUser.setId(keycloakUser.getId());
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.setAssociatedMrds(getAssociatedMrds());
        return ccUser;
    }

    /**
     * Get associated MRDs including all MRDs in the pool.
     */
    private List<AssociatedMrd> getAssociatedMrds() {
        List<AssociatedMrd> associatedMrds = new ArrayList<>();
        this.mrdPool.findAll().forEach(
                mediaRoutingDomain -> associatedMrds.add(
                        new AssociatedMrd(mediaRoutingDomain.getId(), mediaRoutingDomain.getMaxRequests()))
        );
        return associatedMrds;
    }

    /**
     * Agent state.
     *
     * @param request the request
     */
    public void agentState(AgentStateChangeRequest request) {
        Agent agent = this.validateAndGetAgent(request.getAgentId());
        this.agentStateListener.propertyChange(agent, request.getState());
    }

    /**
     * Agent mrd state.
     *
     * @param request the request
     */
    public void agentMrdState(AgentMrdStateChangeRequest request) {
        Agent agent = this.validateAndGetAgent(request.getAgentId());
        this.agentMrdStateListener.propertyChange(agent, request.getMrdId(), request.getState(), true);
    }

    /**
     * Validate and get agent.
     *
     * @param agentId the agent id
     * @return the agent
     */
    Agent validateAndGetAgent(UUID agentId) {
        Agent agent = this.agentsPool.findById(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent: " + agentId + " not found");
        }
        return agent;
    }
}