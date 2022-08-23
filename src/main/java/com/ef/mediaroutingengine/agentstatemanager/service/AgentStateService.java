package com.ef.mediaroutingengine.agentstatemanager.service;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.service.AgentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * The Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(AgentStateService.class);

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
     * The Agents service.
     */
    private final AgentsService agentsService;

    /**
     * Instantiates a new Agent state service.
     *
     * @param agentStateListener    the agent state listener
     * @param agentMrdStateListener the agent mrd state listener
     * @param agentsPool            the agents pool
     * @param agentsService         the agents service
     */
    @Autowired
    public AgentStateService(AgentStateListener agentStateListener, AgentMrdStateListener agentMrdStateListener,
                             AgentsPool agentsPool, AgentsService agentsService) {
        this.agentStateListener = agentStateListener;
        this.agentMrdStateListener = agentMrdStateListener;
        this.agentsPool = agentsPool;
        this.agentsService = agentsService;
    }

    /**
     * Handles the agent login request.
     *
     * @param keycloakUser AgentLoginRequest DTO.
     */
    public void agentLogin(KeycloakUser keycloakUser) {
        logger.info("Request to Login Agent initiated for agent: {}", keycloakUser.getId());

        this.agentsService.createOrUpdate(keycloakUser);

        Agent agent = this.agentsPool.findById(keycloakUser.getId());
        this.agentStateListener.propertyChange(agent, new AgentState(Enums.AgentStateName.LOGIN, null));
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
    Agent validateAndGetAgent(String agentId) {
        Agent agent = this.agentsPool.findById(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent: " + agentId + " not found");
        }
        return agent;
    }
}