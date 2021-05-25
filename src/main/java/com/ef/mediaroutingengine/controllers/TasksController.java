package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tasks")
public class TasksController {
    private final TasksPool tasksPool;

    @Autowired
    public TasksController(TasksPool tasksPool) {
        this.tasksPool = tasksPool;
    }

    /**
     * Returns the list of all tasks if no query parameter is provided. Returns a single task when an 'id'
     * query parameter is provided. If the task is not found for the provided ID, it throws a not-found
     * exception.
     *
     * @param id UUID of a specific task (optional).
     * @return List of all tasks or a specific task against an id.
     */
    @GetMapping("")
    public ResponseEntity<Object> retrieve(@RequestParam Optional<UUID> id) {
        if (id.isPresent()) {
            Task task = this.tasksPool.getTask(id.get());
            if (task != null) {
                TaskDto taskDto = new TaskDto(task);
                return new ResponseEntity<>(taskDto, HttpStatus.OK);
            } else {
                throw new NotFoundException("Task not found in Task pool");
            }
        }
        List<TaskDto> taskDtoList = new ArrayList<>();
        for (Task task: tasksPool.getAllTasks()) {
            taskDtoList.add(new TaskDto(task));
        }
        return new ResponseEntity<>(taskDtoList, HttpStatus.OK);
    }
}
