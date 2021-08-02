package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import java.beans.PropertyChangeEvent;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentMrdStateListenerTest {
    private AgentMrdStateListener listener;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private MrdStateDelegateFactory factory;

    @BeforeEach
    void setUp() {
        this.listener = new AgentMrdStateListener(agentsPool, precisionQueuesPool, agentPresenceRepository,
                jmsCommunicator, factory);
    }

    @Test
    void test_returnWithLoggingError_whenAgentInRequestNotFoundInAgentsPool() {
        PropertyChangeEvent evt = this.getPropertyChangeEvent(UUID.randomUUID(), UUID.randomUUID(),
                Enums.AgentMrdStateName.READY);
        when(this.agentsPool.findById(any(UUID.class))).thenReturn(null);
        this.listener.propertyChange(evt, false);
    }

    @Test
    void test_returnWithLoggingError_whenAgentMrdStateNotFoundInAgent() {
        PropertyChangeEvent evt = this.getPropertyChangeEvent(UUID.randomUUID(), UUID.randomUUID(),
                Enums.AgentMrdStateName.NOT_READY);
        Agent agent = mock(Agent.class);
        when(this.agentsPool.findById(any(UUID.class))).thenReturn(agent);
        when(agent.getAgentMrdState(any(UUID.class))).thenReturn(null);
        this.listener.propertyChange(evt, false);
    }

    private PropertyChangeEvent getPropertyChangeEvent(UUID agentId, UUID mrdId, Enums.AgentMrdStateName state) {
        AgentMrdStateChangeRequest request = new AgentMrdStateChangeRequest(agentId, mrdId, state);
        return new PropertyChangeEvent(this, Enums.EventName.AGENT_MRD_STATE.name(), null, request);
    }
}