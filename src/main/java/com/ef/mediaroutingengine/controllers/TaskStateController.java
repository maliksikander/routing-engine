package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.eventlisteners.taskstate.TaskStateListener;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Task state controller.
 */
@RestController
public class TaskStateController {
    /**
     * The Task state listener.
     */
    private final TaskStateListener taskStateListener;

    /**
     * Instantiates a new Task state controller.
     *
     * @param taskStateListener the task state listener
     */
    @Autowired
    public TaskStateController(TaskStateListener taskStateListener) {
        this.taskStateListener = taskStateListener;
    }

    /**
     * Change task state response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/change-task-state")
    public ResponseEntity<Object> changeTaskState(@RequestBody TaskStateChangeRequest request) {
        Task updatedTask = taskStateListener.propertyChange(request);
        if (updatedTask == null) {
            throw new NotFoundException("No task found for id: " + request.getTaskId());
        }
        return ResponseEntity.ok().body(new TaskDto(updatedTask));
    }
}
