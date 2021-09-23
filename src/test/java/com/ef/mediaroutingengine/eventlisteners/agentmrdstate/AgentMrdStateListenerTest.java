package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.TaskRouter;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.beans.PropertyChangeEvent;
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
    void test_fireStateChangeToTaskSchedulers() {
        MediaRoutingDomain mrd = getNewMrd();
        AgentMrdState agentMrdState = new AgentMrdState(mrd, Enums.AgentMrdStateName.READY);

        List<PrecisionQueue> precisionQueueList = new ArrayList<>();
        precisionQueueList.add(mock(PrecisionQueue.class));
        precisionQueueList.add(mock(PrecisionQueue.class));

        when(precisionQueuesPool.toList()).thenReturn(precisionQueueList);

        when(precisionQueueList.get(0).getMrd()).thenReturn(mrd);
        TaskRouter taskRouterForQueue1 = mock(TaskRouter.class);
        when(precisionQueueList.get(0).getTaskScheduler()).thenReturn(taskRouterForQueue1);

        when(precisionQueueList.get(1).getMrd()).thenReturn(getNewMrd());

        listener.fireStateChangeToTaskSchedulers(agentMrdState);

        verifyNoMoreInteractions(precisionQueueList.get(1));

        ArgumentCaptor<PropertyChangeEvent> arg = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(taskRouterForQueue1, times(1)).propertyChange(arg.capture());
        verifyNoMoreInteractions(taskRouterForQueue1);

        PropertyChangeEvent evt = arg.getValue();
        assertEquals("AGENT_MRD_STATE_" + agentMrdState.getState().name(), evt.getPropertyName());
        assertEquals(agentMrdState, evt.getNewValue());
        assertNull(evt.getOldValue());
    }

    @Test
    void test_publish() throws JMSException, JsonProcessingException {
        Agent agent = getNewAgent();

        AgentPresence agentPresence = mock(AgentPresence.class);
        when(agentPresenceRepository.find(agent.getId().toString())).thenReturn(agentPresence);

        listener.publish(agent);

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
        doNothing().when(listenerSpy).publish(agent);
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

        doNothing().when(listenerSpy).publish(agent);
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

        doNothing().when(listenerSpy).publish(agent);
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

        doNothing().when(listenerSpy).publish(agent);
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

        doNothing().when(listenerSpy).publish(agent);
        listenerSpy.run(agent, agentMrdState.getMrd().getId(), requestedState);

        verify(listenerSpy, times(1)).updateState(any(), any(), any());
        verify(listenerSpy, times(1)).isStateReadyOrActive(any());
        verify(listenerSpy, times(0)).fireStateChangeToTaskSchedulers(any());
    }

    private Agent getNewAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
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