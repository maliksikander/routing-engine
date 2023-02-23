package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    void test_publish() throws JMSException, JsonProcessingException {
        Agent agent = getNewAgent();

        AgentPresence agentPresence = mock(AgentPresence.class);
        when(agentPresenceRepository.find(agent.getId())).thenReturn(agentPresence);

        listener.publish(agent, Enums.JmsEventName.AGENT_STATE_CHANGED, new ArrayList<>());

        verifyNoMoreInteractions(agentPresenceRepository);

        ArgumentCaptor<AgentStateChangedResponse> arg = ArgumentCaptor.forClass(AgentStateChangedResponse.class);
        verify(jmsCommunicator, times(1)).publish(arg.capture(),
                eq(Enums.JmsEventName.AGENT_STATE_CHANGED));
        verifyNoMoreInteractions(jmsCommunicator);
        assertEquals(agentPresence, arg.getValue().getAgentPresence());
        assertFalse(arg.getValue().isAgentStateChanged());
    }

    @Test
    void test_updateState() {
        Agent agent = getNewAgent();
        AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.READY);
        Enums.AgentMrdStateName newState = Enums.AgentMrdStateName.NOT_READY;

        listener.updateState(agent, agentMrdState, newState);

        assertEquals(newState, agentMrdState.getState());

        verify(agentPresenceRepository, times(1))
                .updateAgentMrdStateList(eq(agent.getId()), anyList());
        verifyNoMoreInteractions(agentPresenceRepository);
    }

    @Test
    void test_isStateReadyOrActive() {
        assertTrue(listener.isStateReadyOrActive(Enums.AgentMrdStateName.READY));
        assertTrue(listener.isStateReadyOrActive(Enums.AgentMrdStateName.ACTIVE));

        assertFalse(listener.isStateReadyOrActive(Enums.AgentMrdStateName.LOGOUT));
        assertFalse(listener.isStateReadyOrActive(Enums.AgentMrdStateName.LOGIN));
        assertFalse(listener.isStateReadyOrActive(Enums.AgentMrdStateName.NOT_READY));
        assertFalse(listener.isStateReadyOrActive(Enums.AgentMrdStateName.PENDING_NOT_READY));
        assertFalse(listener.isStateReadyOrActive(Enums.AgentMrdStateName.INTERRUPTED));
        assertFalse(listener.isStateReadyOrActive(Enums.AgentMrdStateName.BUSY));
    }

    @Test
    void testRun_publishStateAndReturn_when_agentMrdStateIsNull() {
        Agent agent = getNewAgent();
        AgentMrdStateListener listenerSpy = spy(listener);
        // No need to test publish, it is already tested
        doNothing().when(listenerSpy).publish(eq(agent), any(), eq(new ArrayList<>()));
        listenerSpy.run(agent, "", Enums.AgentMrdStateName.READY);

        verifyNoInteractions(factory);
    }

    @Test
    void testRun_whenMrdStateIsNotUpdated() {
        AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.READY);
        Agent agent = getNewAgent(agentMrdState);
        Enums.AgentMrdStateName requestedState = Enums.AgentMrdStateName.BUSY;
        AgentMrdStateListener listenerSpy = spy(listener);
        MrdStateDelegate delegate = mock(MrdStateDelegate.class);

        when(factory.getDelegate(requestedState)).thenReturn(delegate);
        when(delegate.getNewState(agent, agentMrdState)).thenReturn(Enums.AgentMrdStateName.READY);

        doNothing().when(listenerSpy).publish(eq(agent), any(), eq(new ArrayList<>()));
        listenerSpy.run(agent, agentMrdState.getMrd().getId(), requestedState);

        verify(listenerSpy, times(0)).updateState(any(), any(), any());
        verify(listenerSpy, times(0)).isStateReadyOrActive(any());
        verify(listenerSpy, times(0)).fireStateChangeToTaskSchedulers(any());
    }

    @Test
    void testRun_whenMrdStateIsUpdated_andNewStateIs_Ready() {
        AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.NOT_READY);
        Agent agent = getNewAgent(agentMrdState);
        Enums.AgentMrdStateName requestedState = Enums.AgentMrdStateName.READY;
        AgentMrdStateListener listenerSpy = spy(listener);
        MrdStateDelegate delegate = mock(MrdStateDelegate.class);

        when(factory.getDelegate(requestedState)).thenReturn(delegate);
        when(delegate.getNewState(agent, agentMrdState)).thenReturn(Enums.AgentMrdStateName.READY);

        List<String> mrdStateChanges = new ArrayList<>();
        mrdStateChanges.add(agentMrdState.getMrd().getId());

        doNothing().when(listenerSpy).publish(eq(agent), any(), eq(mrdStateChanges));
        listenerSpy.run(agent, agentMrdState.getMrd().getId(), requestedState);

        verify(listenerSpy, times(1)).updateState(any(), any(), any());
        verify(listenerSpy, times(1)).isStateReadyOrActive(any());
        verify(listenerSpy, times(1)).fireStateChangeToTaskSchedulers(any());
    }

    @Test
    void testRun_whenMrdStateIsUpdated_andNewStateIs_Active() {
        AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.READY);
        Agent agent = getNewAgent(agentMrdState);
        Enums.AgentMrdStateName requestedState = Enums.AgentMrdStateName.ACTIVE;
        AgentMrdStateListener listenerSpy = spy(listener);
        MrdStateDelegate delegate = mock(MrdStateDelegate.class);

        when(factory.getDelegate(requestedState)).thenReturn(delegate);
        when(delegate.getNewState(agent, agentMrdState)).thenReturn(Enums.AgentMrdStateName.ACTIVE);

        List<String> mrdStateChanges = new ArrayList<>();
        mrdStateChanges.add(agentMrdState.getMrd().getId());

        doNothing().when(listenerSpy).publish(eq(agent), any(), eq(mrdStateChanges));
        listenerSpy.run(agent, agentMrdState.getMrd().getId(), requestedState);

        verify(listenerSpy, times(1)).updateState(any(), any(), any());
        verify(listenerSpy, times(1)).isStateReadyOrActive(any());
        verify(listenerSpy, times(1)).fireStateChangeToTaskSchedulers(any());
    }

    @Test
    void testRun_whenMrdStateIsUpdated_andNewStateIsNot_ReadyOrActive() {
        AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.ACTIVE);
        Agent agent = getNewAgent(agentMrdState);
        Enums.AgentMrdStateName requestedState = Enums.AgentMrdStateName.NOT_READY;
        AgentMrdStateListener listenerSpy = spy(listener);
        MrdStateDelegate delegate = mock(MrdStateDelegate.class);

        when(factory.getDelegate(requestedState)).thenReturn(delegate);
        when(delegate.getNewState(agent, agentMrdState)).thenReturn(Enums.AgentMrdStateName.PENDING_NOT_READY);

        List<String> mrdStateChanges = new ArrayList<>();
        mrdStateChanges.add(agentMrdState.getMrd().getId());

        doNothing().when(listenerSpy).publish(eq(agent), any(), eq(mrdStateChanges));
        listenerSpy.run(agent, agentMrdState.getMrd().getId(), requestedState);

        verify(listenerSpy, times(1)).updateState(any(), any(), any());
        verify(listenerSpy, times(1)).isStateReadyOrActive(any());
        verify(listenerSpy, times(0)).fireStateChangeToTaskSchedulers(any());
    }

    private Agent getNewAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }

    private Agent getNewAgent(AgentMrdState agentMrdState) {
        Agent agent = getNewAgent();
        agent.addAgentMrdState(agentMrdState);
        return agent;
    }

    private MediaRoutingDomain getNewMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        return mrd;
    }
}