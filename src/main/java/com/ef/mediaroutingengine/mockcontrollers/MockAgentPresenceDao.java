package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockAgentPresenceDao {
    private final AgentPresenceRepository agentPresenceRepository;

    @Autowired
    public MockAgentPresenceDao(AgentPresenceRepository agentPresenceRepository) {
        this.agentPresenceRepository = agentPresenceRepository;
    }

    @GetMapping("/agent-presence")
    public ResponseEntity<Object> retrieve() {
        return new ResponseEntity<>(this.agentPresenceRepository.findAll(), HttpStatus.OK);
    }
}
