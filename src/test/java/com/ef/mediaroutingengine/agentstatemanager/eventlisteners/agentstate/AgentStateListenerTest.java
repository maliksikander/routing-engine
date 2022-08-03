package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import javax.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentStateListenerTest {
    private AgentStateListener agentStateListener;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private AgentStateDelegateFactory factory;

    @BeforeEach
    void setUp() {
        agentStateListener = new AgentStateListener(agentPresenceRepository, jmsCommunicator, factory);
    }

    @Test
    void testRun_doesNothing_when_delegateIsNull() {
        AgentState newState = new AgentState(null, null);
        Agent agent = getAgent();

        when(factory.getDelegate(newState.getName())).thenReturn(null);
        this.agentStateListener.run(agent, newState);

        verify(factory, times(1)).getDelegate(any());
        verifyNoInteractions(jmsCommunicator);
    }

    @Test
    void testRun_delegateUpdatesStateAndStateChangePublishedOnJms_when_delegateIsNotNull()
            throws JMSException, JsonProcessingException {
        AgentState newState = new AgentState(Enums.AgentStateName.READY, null);
        Agent agent = getAgent();
        AgentStateDelegate delegate = mock(AgentStateDelegate.class);

        when(factory.getDelegate(newState.getName())).thenReturn(delegate);
        this.agentStateListener.run(agent, newState);

        verify(factory, times(1)).getDelegate(any());
        verify(delegate, times(1)).updateState(agent, newState);
        verify(jmsCommunicator, times(1)).publish(any(), any());
    }

    private Agent getAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        Agent agent = new Agent(ccUser);
        agent.setState(new AgentState(Enums.AgentStateName.LOGOUT, null));
        return agent;
    }
}
