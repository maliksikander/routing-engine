package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentStateLogoutTest {
    private AgentStateLogout agentStateLogout;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private TaskManager taskManager;
    @Mock
    private JmsCommunicator jmsCommunicator;

    @BeforeEach
    void setUp() {
        this.agentStateLogout = new AgentStateLogout(agentPresenceRepository, taskManager, jmsCommunicator);
    }

    @Test
    void test_handleActiveTasks() {
        Agent agent = mock(Agent.class);

        List<Task> activeTasks = new ArrayList<>();
        activeTasks.add(mock(Task.class));
        activeTasks.add(mock(Task.class));

        when(agent.getActiveTasksList()).thenReturn(activeTasks);

        this.agentStateLogout.handleActiveTasks(agent);

        verify(this.taskManager, times(activeTasks.size())).removeFromPoolAndRepository(any());
        verify(this.jmsCommunicator, times(activeTasks.size())).publishTaskStateChangeForReporting(any());
        verifyNoMoreInteractions(this.taskManager);
    }

    @Test
    void test_handleReservedTask_when_reservedTaskIsNotNull() {
        Agent agent = mock(Agent.class);
        Task reservedTask = mock(Task.class);

        when(agent.getReservedTask()).thenReturn(reservedTask);

        this.agentStateLogout.handleReservedTasks(agent);

        verify(this.taskManager, times(1)).removeFromPoolAndRepository(reservedTask);
        verify(this.jmsCommunicator, times(1)).publishTaskStateChangeForReporting(any());
        verify(this.taskManager, times(1)).rerouteReservedTask(reservedTask);

        verifyNoMoreInteractions(this.taskManager);

        verifyNoMoreInteractions(agent);
    }

    @Test
    void test_handleAgentTasks() {
        Agent agent = mock(Agent.class);
        AgentStateLogout spy = Mockito.spy(agentStateLogout);

        spy.handleAgentTasks(agent);

        verify(spy, times(1)).handleReservedTasks(agent);
        verify(spy, times(1)).handleActiveTasks(agent);
        verify(agent, times(1)).clearAllTasks();
    }

    @Test
    void test_updateState() {
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.READY));
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Voice"), Enums.AgentMrdStateName.ACTIVE));

        Agent agent = getNewAgent();
        agent.setAgentMrdStates(agentMrdStateList);

        AgentState newState = new AgentState(Enums.AgentStateName.LOGOUT, null);
        AgentStateLogout spy = Mockito.spy(agentStateLogout);

        doNothing().when(spy).handleAgentTasks(agent);
        AgentStateChangedResponse res = spy.updateState(agent, newState,false);

        // Assert agent-state has been updated to the new state i.e. LOGOUT
        assertEquals(newState.getName(), agent.getState().getName());
        // Assert all agent-mrd-states have been updated to log-out
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            assertEquals(Enums.AgentMrdStateName.LOGOUT, agentMrdState.getState());
        }
        // Verify agentPresenceRepository calls are made correctly
        verify(this.agentPresenceRepository, times(1))
                .updateAgentState(agent.getId(), newState);
        verify(this.agentPresenceRepository, times(1))
                .updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
        verifyNoMoreInteractions(this.agentPresenceRepository);

        assertTrue(res.isAgentStateChanged());
    }

    private MediaRoutingDomain getNewMrd(String name) {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName(name);
        mrd.setDescription(name + " Description");
        return mrd;
    }

    private Agent getNewAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }
}