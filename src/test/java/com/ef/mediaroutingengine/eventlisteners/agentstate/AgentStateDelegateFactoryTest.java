package com.ef.mediaroutingengine.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ef.mediaroutingengine.commons.Enums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentStateDelegateFactoryTest {
    private AgentStateDelegateFactory factory;
    @Mock
    private AgentStateNotReady agentStateNotReady;
    @Mock
    private AgentStateReady agentStateReady;
    @Mock
    private AgentStateLogin agentStateLogin;
    @Mock
    private AgentStateLogout agentStateLogout;

    @BeforeEach
    void setUp() {
        this.factory = new AgentStateDelegateFactory(agentStateNotReady, agentStateReady, agentStateLogin,
                agentStateLogout);
    }

    @Test
    void testGetDelegate_returnsAgentStateNotReadyDelegate_when_requiredStateIsNotReady() {
        AgentStateDelegate delegate = factory.getDelegate(Enums.AgentStateName.NOT_READY);
        assertEquals(delegate, agentStateNotReady);
    }

    @Test
    void testGetDelegate_returnsAgentStateReadyDelegate_when_requiredStateIsReady() {
        AgentStateDelegate delegate = factory.getDelegate(Enums.AgentStateName.READY);
        assertEquals(delegate, agentStateReady);
    }

    @Test
    void testGetDelegate_returnsAgentStateLoginDelegate_when_requiredStateIsLogin() {
        AgentStateDelegate delegate = factory.getDelegate(Enums.AgentStateName.LOGIN);
        assertEquals(delegate, agentStateLogin);
    }

    @Test
    void testGetDelegate_returnsAgentStateLogoutDelegate_when_requiredStateIsLogout() {
        AgentStateDelegate delegate = factory.getDelegate(Enums.AgentStateName.LOGOUT);
        assertEquals(delegate, agentStateLogout);
    }

    @Test
    void testGetDelegate_returnsNull_when_requiredStateIsNull() {
        assertNull(factory.getDelegate(null));
    }
}
