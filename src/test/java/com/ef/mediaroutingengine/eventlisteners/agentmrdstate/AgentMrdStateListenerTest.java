package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentMrdStateListenerTest {
    private AgentMrdStateListener listener;
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
        this.listener = new AgentMrdStateListener(precisionQueuesPool, agentPresenceRepository,
                jmsCommunicator, factory);
    }
}