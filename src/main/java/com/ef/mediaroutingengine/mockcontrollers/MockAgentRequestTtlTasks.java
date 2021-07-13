package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.services.pools.TasksPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockAgentRequestTtlTasks {
    private final TasksPool tasksPool;

    @Autowired
    public MockAgentRequestTtlTasks(TasksPool tasksPool) {
        this.tasksPool = tasksPool;
    }

    @GetMapping("/get-all-timer-tasks")
    public ResponseEntity<Object> getAllTimerTasks() {
        return new ResponseEntity<>(this.tasksPool.getAllActiveTimers(), HttpStatus.OK);
    }
}
