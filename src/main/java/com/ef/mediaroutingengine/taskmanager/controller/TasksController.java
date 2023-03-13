package com.ef.mediaroutingengine.taskmanager.controller;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.taskmanager.dto.UpdateTaskRequest;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.service.TasksService;
import com.ef.mediaroutingengine.taskmanager.service.taskstate.TaskStateListener;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
    @CrossOrigin(origins = "*")
    @GetMapping("/{id}")
    public ResponseEntity<Object> retrieveById(@PathVariable String id) {
        return new ResponseEntity<>(this.service.retrieveById(id), HttpStatus.OK);
    }

    /**
     * Retrieve response entity.
     *
     * @param agentId   the agent id
     * @param taskState the task state
     * @return the response entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping("")
    public ResponseEntity<Object> retrieve(@RequestParam Optional<String> agentId,
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
    @CrossOrigin(origins = "*")
    @PostMapping("/{taskId}/change-state")
    public ResponseEntity<Object> changeTaskState(@PathVariable String taskId,
                                                  @Valid @RequestBody TaskState request) {
        Task updatedTask = taskStateListener.propertyChange(taskId, request);
        if (updatedTask == null) {
            throw new NotFoundException("No task found for id: " + taskId);
        }
        return ResponseEntity.ok().body(AdapterUtility.createTaskDtoFrom(updatedTask));
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/{taskId}/update")
    public ResponseEntity<Object> updateTask(@PathVariable String taskId,
                                             @RequestBody UpdateTaskRequest reqBody) {
        return ResponseEntity.ok().body(this.service.updateTask(taskId, reqBody));
    }
}
