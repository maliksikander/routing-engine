package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentLoginRequest;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
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
     * Instantiates a new Agent state service.
     *
     * @param agentStateListener    the agent state listener
     * @param agentMrdStateListener the agent mrd state listener
     * @param agentsPool            the agents pool
     */
    @Autowired
    public AgentStateService(AgentStateListener agentStateListener, AgentMrdStateListener agentMrdStateListener,
                             AgentsPool agentsPool) {
        this.agentStateListener = agentStateListener;
        this.agentMrdStateListener = agentMrdStateListener;
        this.agentsPool = agentsPool;
    }

    /**
     * Handles the agent login request.
     *
     * @param request AgentLoginRequest DTO.
     */
    public void agentLogin(AgentLoginRequest request) {
        this.agentState(request.getAgentId(), new AgentState(Enums.AgentStateName.LOGIN, null));
    }

    /**
     * Agent state.
     *
     * @param request the request
     */
    public void agentState(AgentStateChangeRequest request) {
        this.agentState(request.getAgentId(), request.getState());
    }

    /**
     * Agent state.
     *
     * @param agentId    the agent id
     * @param agentState the agent state
     */
    private void agentState(UUID agentId, AgentState agentState) {
        Agent agent = this.validateAndGetAgent(agentId);
        this.agentStateListener.propertyChange(agent, agentState);
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
     * Validate and get agent agent.
     *
     * @param agentId the agent id
     * @return the agent
     */
    private Agent validateAndGetAgent(UUID agentId) {
        Agent agent = this.agentsPool.findById(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent: " + agentId + " not found");
        }
        return agent;
    }
}