package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.services.controllerservices.taskservice.TasksService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
     * Instantiates a new Tasks controller.
     *
     * @param service the service
     */
    @Autowired
    public TasksController(TasksService service) {
        this.service = service;
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
}
