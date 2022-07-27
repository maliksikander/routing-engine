package com.ef.mediaroutingengine.taskmanager.controller;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.taskmanager.dto.PullAssignTaskRequest;
import com.ef.mediaroutingengine.taskmanager.dto.UpdateTaskRequest;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.service.TasksService;
import com.ef.mediaroutingengine.taskmanager.service.taskstate.TaskStateListener;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Get Tasks Rest Controller.
 */
@RestController
@RequestMapping("tasks")
public class TasksController {
    /**
     * The Service.
     */
    private final TasksService service;
    /**
     * The Task state listener.
     */
    private final TaskStateListener taskStateListener;
    private final AgentsPool agentsPool;
    private final MrdPool mrdPool;

    /**
     * Instantiates a new Tasks controller.
     *
     * @param service the service
     */
    @Autowired
    public TasksController(TasksService service, TaskStateListener taskStateListener,
                           AgentsPool agentsPool, MrdPool mrdPool) {
        this.service = service;
        this.taskStateListener = taskStateListener;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
    }

    /**
     * Retrieve by id response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> retrieveById(@PathVariable UUID id) {
        return new ResponseEntity<>(this.service.retrieveById(id), HttpStatus.OK);
    }

    /**
     * Retrieve response entity.
     *
     * @param agentId   the agent id
     * @param taskState the task state
     * @return the response entity
     */
    @GetMapping("")
    public ResponseEntity<Object> retrieve(@RequestParam Optional<UUID> agentId,
                                           @RequestParam Optional<Enums.TaskStateName> taskState) {
        return new ResponseEntity<>(this.service.retrieve(agentId, taskState), HttpStatus.OK);
    }

    /**
     * Change task state response entity.
     *
     * @param taskId  the task id
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/{taskId}/change-state")
    public ResponseEntity<Object> changeTaskState(@PathVariable UUID taskId,
                                                  @Valid @RequestBody TaskState request) {
        Task updatedTask = taskStateListener.propertyChange(taskId, request);
        if (updatedTask == null) {
            throw new NotFoundException("No task found for id: " + taskId);
        }
        return ResponseEntity.ok().body(AdapterUtility.createTaskDtoFrom(updatedTask));
    }

    @PostMapping("/{taskId}/update")
    public ResponseEntity<Object> updateTask(@PathVariable UUID taskId,
                                             @RequestBody UpdateTaskRequest reqBody) {
        return ResponseEntity.ok().body(this.service.updateTask(taskId, reqBody));
    }

    /**
     * Assign task response entity.
     *
     * @param reqBody the request body
     * @return the response entity
     */
    @PostMapping("/assign")
    public ResponseEntity<Object> assignTask(@Valid @RequestBody PullAssignTaskRequest reqBody) {
        Agent agent = this.validateAndGetAgent(reqBody.getAgentId());
        MediaRoutingDomain mrd = this.validateAndGetMrd(reqBody.getMrdId());
        validateAgentHasMrdState(agent, mrd);
        validateChannelSession(reqBody.getChannelSession());

        return ResponseEntity.ok().body(this.service.assignTask(agent, mrd, reqBody.getTaskState(),
                reqBody.getChannelSession()));
    }

    private void validateChannelSession(ChannelSession channelSession) {
        RoutingMode routingMode = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getRoutingMode();
        if (routingMode == null || routingMode.equals(RoutingMode.PUSH)) {
            throw new IllegalArgumentException("Invalid Routing mode in channelSession, It should be Pull or External");
        }
    }

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

    private MediaRoutingDomain validateAndGetMrd(String mrdId) {
        MediaRoutingDomain mrd = this.mrdPool.findById(mrdId);
        if (mrd == null) {
            throw new NotFoundException("MRD: " + mrdId + " in requested channel session not found");
        }
        return mrd;
    }

    private void validateAgentHasMrdState(Agent agent, MediaRoutingDomain mrd) {
        if (agent.getAgentMrdState(mrd.getId()) == null) {
            throw new NotFoundException("Agent: " + agent.getId() + " does not have AgentMrdState for MRD: "
                    + mrd.getId());
        }
    }
}
