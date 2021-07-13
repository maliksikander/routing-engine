package com.ef.mediaroutingengine.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentLoginRequestTest {
    private AgentLoginRequest agentLoginRequest;

    @BeforeEach
    void setUp() {
        this.agentLoginRequest = new AgentLoginRequest();
    }

    @Test
    void testGetAgentId_returnsAgentId() {
        assertNull(this.agentLoginRequest.getAgentId());
        UUID agentId = UUID.randomUUID();
        this.agentLoginRequest.setAgentId(agentId);
        assertEquals(agentId, this.agentLoginRequest.getAgentId());
    }

    @Test
    void testSetAgentId_setsAgentId() {
        UUID agentId = UUID.randomUUID();
        this.agentLoginRequest.setAgentId(agentId);
        assertEquals(agentId, this.agentLoginRequest.getAgentId());
        this.agentLoginRequest.setAgentId(null);
        assertNull(this.agentLoginRequest.getAgentId());
    }
}
