package com.ef.mediaroutingengine.routing.controller;

import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Test controller.
 */
@RestController
public class TestController {
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;

    /**
     * Instantiates a new Test controller.
     *
     * @param agentsPool          the agents pool
     * @param precisionQueuesPool the precision queues pool
     */
    @Autowired
    public TestController(AgentsPool agentsPool, PrecisionQueuesPool precisionQueuesPool) {
        this.agentsPool = agentsPool;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    /**
     * Gets in memory agents.
     *
     * @return the in memory agents
     */
    @GetMapping("in-memory-agents")
    public ResponseEntity<Object> getInMemoryAgents() {
        return ResponseEntity.ok().body(this.agentsPool.findAll());
    }

    /**
     * Gets in memory queue tasks.
     *
     * @param id the id
     * @return the in memory queue tasks
     */
    @GetMapping("queue/{id}/queue-tasks")
    public ResponseEntity<Object> getInMemoryQueueTasks(@PathVariable String id) {
        PrecisionQueue queue = this.precisionQueuesPool.findById(id);

        if (queue == null) {
            return ResponseEntity.ok().body(new ArrayList<>());
        }

        return ResponseEntity.ok().body(queue.getTasks());
    }
}
