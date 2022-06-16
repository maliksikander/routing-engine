package com.ef.mediaroutingengine.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.ef.cim.objectmodel.ReasonCode;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
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
class AgentStateNotReadyTest {
    private AgentStateNotReady agentStateNotReady;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;

    @BeforeEach
    void setUp() {
        this.agentStateNotReady = new AgentStateNotReady(agentPresenceRepository);
    }

    @Test
    void testUpdateAgentMrdStates() {
        Agent agent = mock(Agent.class);
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.READY));
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Voice"), Enums.AgentMrdStateName.NOT_READY));
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Email"), Enums.AgentMrdStateName.ACTIVE));

        when(agent.getAgentMrdStates()).thenReturn(agentMrdStateList);
        when(agent.getNoOfActivePushTasks(agentMrdStateList.get(0).getMrd().getId())).thenReturn(0);
        when(agent.getNoOfActivePushTasks(agentMrdStateList.get(1).getMrd().getId())).thenReturn(0);
        when(agent.getNoOfActivePushTasks(agentMrdStateList.get(2).getMrd().getId())).thenReturn(2);

        this.agentStateNotReady.updateAgentMrdStates(agent);

        assertEquals(Enums.AgentMrdStateName.NOT_READY, agentMrdStateList.get(0).getState());
        assertEquals(Enums.AgentMrdStateName.NOT_READY, agentMrdStateList.get(1).getState());
        assertEquals(Enums.AgentMrdStateName.PENDING_NOT_READY, agentMrdStateList.get(2).getState());
    }

    @Test
    void test_updateReadyStateToNotReady() {
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.NOT_READY));

        Agent agent = getNewAgent();
        agent.setAgentMrdStates(agentMrdStateList);

        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, null);
        AgentStateNotReady spy = Mockito.spy(agentStateNotReady);

        // No need to test the updateAgentMrdStates. it is already tested.
        when(spy.updateAgentMrdStates(agent)).thenReturn(agentMrdStateList);
        spy.updateReadyStateToNotReady(agent, newState);

        // Assert that new state has been set for the agent
        assertEquals(newState, agent.getState());
        // verify correct calls to the repository are made
        verify(agentPresenceRepository, times(1))
                .updateAgentState(agent.getId(), newState);
        verify(agentPresenceRepository, times(1))
                .updateAgentMrdStateList(agent.getId(), agentMrdStateList);
        verifyNoMoreInteractions(this.agentPresenceRepository);
    }

    @Test
    void testUpdateState_updatesStateAndReturnsTrue_when_currentStateIsReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.READY, null));

        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, null);

        AgentStateNotReady spy = Mockito.spy(agentStateNotReady);
        // no need to test readyToNotReady method again, it is already tested.
        doNothing().when(spy).updateReadyStateToNotReady(agent, newState);

        assertTrue(spy.updateState(agent, newState));
    }

    @Test
    void testUpdateState_updatesStateAndReturnsTrue_when_currentStateIsNotReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.NOT_READY, null));

        ReasonCode reasonCode = new ReasonCode("Lunch break", Enums.ReasonCodeType.NOT_READY);
        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, reasonCode);

        boolean isStateUpdated = this.agentStateNotReady.updateState(agent, newState);
        // Assert that agent's state is updated to new state
        assertEquals(newState, agent.getState());
        // Verify that correct repository calls are made.
        verify(this.agentPresenceRepository, times(1))
                .updateAgentState(agent.getId(), newState);
        verifyNoMoreInteractions(this.agentPresenceRepository);
        // Assert return value is true.
        assertTrue(isStateUpdated);
    }

    @Test
    void testUpdateState_returnsFalse_when_currentStateIsNotReadyOrNotReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.LOGIN, null));

        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, null);

        assertFalse(this.agentStateNotReady.updateState(agent, newState));

        agent.setState(new AgentState(Enums.AgentStateName.LOGOUT, null));
        assertFalse(this.agentStateNotReady.updateState(agent, newState));
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