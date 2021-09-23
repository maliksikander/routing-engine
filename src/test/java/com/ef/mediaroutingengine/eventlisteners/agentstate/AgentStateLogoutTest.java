package com.ef.mediaroutingengine.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
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

    @BeforeEach
    void setUp() {
        this.agentStateLogout = new AgentStateLogout(agentPresenceRepository, taskManager);
    }

    @Test
    void test_handleAgentTasks() {
        Agent agent = mock(Agent.class);

        List<Task> taskList = new ArrayList<>();
        taskList.add(mock(Task.class));
        taskList.add(mock(Task.class));

        when(agent.getAllTasks()).thenReturn(taskList);
        when(taskList.get(0).getRoutingMode()).thenReturn(RoutingMode.PUSH);
        when(taskList.get(1).getRoutingMode()).thenReturn(RoutingMode.PULL);

        this.agentStateLogout.handleAgentTasks(agent);

        verify(this.taskManager, times(1)).rerouteTask(taskList.get(0));
        verify(this.taskManager, times(1)).removePullTaskOnAgentLogout(taskList.get(1));
        verifyNoMoreInteractions(this.taskManager);
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
        boolean isStateChanged = spy.updateState(agent, newState);

        // Assert agent-state has been updated to the new state i.e. LOGOUT
        assertEquals(newState.getName(), agent.getState().getName());
        // Assert all agent-mrd-states have been updated to log-out
        for (AgentMrdState agentMrdState: agent.getAgentMrdStates()) {
            assertEquals(Enums.AgentMrdStateName.LOGOUT, agentMrdState.getState());
        }
        // Verify agentPresenceRepository calls are made correctly
        verify(this.agentPresenceRepository, times(1))
                .updateAgentState(agent.getId(), newState);
        verify(this.agentPresenceRepository, times(1))
                .updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
        verifyNoMoreInteractions(this.agentPresenceRepository);

        assertTrue(isStateChanged);
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
        keycloakUser.setId(UUID.randomUUID());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }
}