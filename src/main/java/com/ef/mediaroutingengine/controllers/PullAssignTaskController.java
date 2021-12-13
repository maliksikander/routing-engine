package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.PullAssignTaskRequest;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.controllerservices.PullAssignTaskService;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Pull assign task controller.
 */
@RestController
public class PullAssignTaskController {
    /**
     * The Service.
     */
    private final PullAssignTaskService service;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;

    /**
     * Instantiates a new Pull assign task controller.
     *
     * @param service    the service
     * @param agentsPool the agents pool
     * @param mrdPool    the mrd pool
     */
    @Autowired
    public PullAssignTaskController(PullAssignTaskService service,
                                    AgentsPool agentsPool, MrdPool mrdPool) {
        this.service = service;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
    }

    /**
     * Assign task response entity.
     *
     * @param requestBody the request body
     * @return the response entity
     */
    @PostMapping("assign-task")
    public ResponseEntity<Object> assignTask(@Valid @RequestBody PullAssignTaskRequest requestBody) {
        validateRoutingMode(requestBody.getChannelSession());
        Agent agent = validateAndGetAgent(requestBody.getAgentId());
        MediaRoutingDomain mrd = validateAndGetMrd(requestBody.getChannelSession());
        validateAgentHasMrdState(agent, mrd);
        TaskDto taskDto = this.service.assignTask(agent, mrd, requestBody.getChannelSession());
        return new ResponseEntity<>(taskDto, HttpStatus.OK);
    }

    /**
     * Validate routing mode.
     *
     * @param channelSession the channel session
     */
    private void validateRoutingMode(ChannelSession channelSession) {
        if (!channelSession.getChannel().getChannelConfig().getRoutingPolicy()
                .getRoutingMode().equals(RoutingMode.PULL)) {
            throw new IllegalArgumentException("The routing mode in RoutingPolicy must be PULL for this API");
        }
    }

    /**
     * Validate and get agent.
     *
     * @param agentId the agent id
     * @return the agent
     */
    private Agent validateAndGetAgent(UUID agentId) {
        Agent agent = this.agentsPool.findById(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent: " + agentId + " not found");
        }
        if (agent.getState().getName().equals(Enums.AgentStateName.LOGOUT)) {
            throw new IllegalStateException("Cannot Assign task when agent is in LOGOUT state");
        }
        return agent;
    }

    /**
     * Validate and get mrd media routing domain.
     *
     * @param channelSession the channel session
     * @return the media routing domain
     */
    private MediaRoutingDomain validateAndGetMrd(ChannelSession channelSession) {
        String mrdId = channelSession.getChannel().getChannelType().getMediaRoutingDomain();
        MediaRoutingDomain mrd = this.mrdPool.findById(mrdId);
        if (mrd == null) {
            throw new NotFoundException("MRD: " + mrdId + " in requested channel session not found");
        }
        return mrd;
    }

    /**
     * Validate agent has mrd state.
     *
     * @param agent the agent
     * @param mrd   the mrd
     */
    private void validateAgentHasMrdState(Agent agent, MediaRoutingDomain mrd) {
        if (agent.getAgentMrdState(mrd.getId()) == null) {
            throw new NotFoundException("Agent: " + agent.getId() + " does not have AgentMrdState for MRD: "
            + mrd.getId());
        }
    }
}
