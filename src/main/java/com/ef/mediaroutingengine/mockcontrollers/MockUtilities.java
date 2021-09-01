package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock utilities.
 */
@RestController
public class MockUtilities {
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;

    /**
     * Instantiates a new Mock utilities.
     *
     * @param agentsPool the agents pool
     */
    @Autowired
    public MockUtilities(AgentsPool agentsPool) {
        this.agentsPool = agentsPool;
    }

    /**
     * API to get number of tasks of an agent.
     *
     * @param agentId id of the agent.
     * @param mrdId   id of the mrd.
     * @return the number of tasks of an agent
     */
    @GetMapping("/no-of-tasks")
    public ResponseEntity<Object> getNoOfTasks(@RequestParam UUID agentId, @RequestParam String mrdId) {
        Agent agent = this.agentsPool.findById(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent not found with id: " + agentId);
        }
        if (agent.getAgentMrdState(mrdId) == null) {
            throw new NotFoundException("MRD: " + mrdId + " not associated with agent");
        }
        int noOfTasks = agent.getNoOfActiveTasks(mrdId);
        return new ResponseEntity<>("No of tasks: " + noOfTasks, HttpStatus.OK);
    }
}
