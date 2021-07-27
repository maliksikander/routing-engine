package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.services.pools.TasksPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock agent request ttl tasks.
 */
@RestController
public class MockAgentRequestTtlTasks {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    /**
     * Instantiates a new Mock agent request ttl tasks.
     *
     * @param tasksPool the tasks pool
     */
    @Autowired
    public MockAgentRequestTtlTasks(TasksPool tasksPool) {
        this.tasksPool = tasksPool;
    }

    /**
     * Gets all timer tasks.
     *
     * @return the all timer tasks
     */
    @GetMapping("/get-all-timer-tasks")
    public ResponseEntity<Object> getAllTimerTasks() {
        return new ResponseEntity<>(this.tasksPool.getAllActiveTimers(), HttpStatus.OK);
    }
}
