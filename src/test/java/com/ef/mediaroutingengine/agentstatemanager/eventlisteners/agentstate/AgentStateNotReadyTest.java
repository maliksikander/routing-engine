package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
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
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
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
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private MrdPool mrdPool;

    private AgentStateNotReady agentStateNotReady;

    @BeforeEach
    void setUp() {
        this.agentStateNotReady = new AgentStateNotReady(agentPresenceRepository, mrdPool);
    }

    @Test
    void testUpdateAgentMrdStates() {
        Agent agent = mock(Agent.class);
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.READY));
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Voice"), Enums.AgentMrdStateName.NOT_READY));
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Email"), Enums.AgentMrdStateName.ACTIVE));

        when(agent.getAgentMrdStates()).thenReturn(agentMrdStateList);
        when(agent.getNoOfActiveQueueTasks(agentMrdStateList.get(0).getMrd().getId())).thenReturn(0);
        when(agent.getNoOfActiveQueueTasks(agentMrdStateList.get(1).getMrd().getId())).thenReturn(0);
        when(agent.getNoOfActiveQueueTasks(agentMrdStateList.get(2).getMrd().getId())).thenReturn(2);

        this.agentStateNotReady.updateAgentMrdStates(agent, null, false);

        assertEquals(Enums.AgentMrdStateName.NOT_READY, agentMrdStateList.get(0).getState());
        assertEquals(Enums.AgentMrdStateName.NOT_READY, agentMrdStateList.get(1).getState());
        assertEquals(Enums.AgentMrdStateName.PENDING_NOT_READY, agentMrdStateList.get(2).getState());
    }

    @Test
    void testUpdateState_updatesStateAndReturnsTrue_when_currentStateIsReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.READY, null));

        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, null);

        AgentStateNotReady spy = Mockito.spy(agentStateNotReady);

        doReturn(new ArrayList<>()).when(spy).updateAgentMrdStates(agent, null, false);
        doReturn(false).when(spy).isAnyMrdInAvailableState(agent);

        AgentStateChangedResponse res = spy.updateState(agent, newState, false);

        verify(agentPresenceRepository, times(1)).updateAgentState(agent.getId(), newState);
        assertTrue(res.isAgentStateChanged());
    }

    @Test
    void testUpdateState_updatesStateAndReturnsTrue_when_currentStateIsNotReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.NOT_READY, null));

        ReasonCode reasonCode = new ReasonCode("Lunch break", Enums.ReasonCodeType.NOT_READY);
        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, reasonCode);

        AgentStateChangedResponse res = this.agentStateNotReady.updateState(agent, newState, false);
        // Assert that agent's state is updated to new state
        assertEquals(newState, agent.getState());
        // Verify that correct repository calls are made.
        verify(this.agentPresenceRepository, times(1)).updateAgentState(agent.getId(), newState);
        verifyNoMoreInteractions(this.agentPresenceRepository);
        // Assert return value is true.
        assertTrue(res.isAgentStateChanged());
    }

    @Test
    void testUpdateState_returnsFalse_when_currentStateIsNotReadyOrNotReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.LOGIN, null));

        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, null);

        assertFalse(this.agentStateNotReady.updateState(agent, newState, false).isAgentStateChanged());

        agent.setState(new AgentState(Enums.AgentStateName.LOGOUT, null));
        assertFalse(this.agentStateNotReady.updateState(agent, newState, false).isAgentStateChanged());
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