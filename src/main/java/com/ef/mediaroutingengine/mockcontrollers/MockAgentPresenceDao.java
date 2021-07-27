package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock agent presence dao.
 */
@RestController
public class MockAgentPresenceDao {
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;

    /**
     * Instantiates a new Mock agent presence dao.
     *
     * @param agentPresenceRepository the agent presence repository
     */
    @Autowired
    public MockAgentPresenceDao(AgentPresenceRepository agentPresenceRepository) {
        this.agentPresenceRepository = agentPresenceRepository;
    }

    /**
     * Retrieve response entity.
     *
     * @return the response entity
     */
    @GetMapping("/agent-presence")
    public ResponseEntity<Object> retrieve() {
        return new ResponseEntity<>(this.agentPresenceRepository.findAll(), HttpStatus.OK);
    }
}
