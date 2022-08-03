package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.routing.model.Agent;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MrdStateReadyTest {
    MrdStateReady mrdStateReady = new MrdStateReady();

    @Nested
    @DisplayName("getNewState method, returns current mrd-state")
    class MrdStateNotUpdated {
        @Test
        void returnsCurrentMrdState_when_agentStateIsLogout() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.LOGOUT, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.ACTIVE);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsCurrentMrdState_when_agentStateIsLogin() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.LOGIN, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.ACTIVE);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsCurrentMrdState_when_agentStateIsNotReady() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.NOT_READY, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.ACTIVE);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsCurrentMrdState_when_agentStateIsReady_currentMrdStateIsLogout() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.LOGOUT);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsCurrentMrdState_when_agentStateIsReady_currentMrdStateIsLogin() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.LOGIN);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsCurrentMrdState_when_agentStateIsReady_currentMrdStateIsReady() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.READY);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsCurrentMrdState_when_agentStateIsReady_currentMrdStateIsInterrupted() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.INTERRUPTED);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsCurrentMrdState_when_agentStateIsReady_currentMrdStateIsActive_atLeastOneActivePushTaskForMrd() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            Agent agentSpy = spy(agent);
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.ACTIVE);
            when(agentSpy.getNoOfActivePushTasks(agentMrdState.getMrd().getId())).thenReturn(1);
            assertEquals(agentMrdState.getState(), mrdStateReady.getNewState(agentSpy, agentMrdState));
        }
    }

    @Nested
    @DisplayName("getNewState method, returns updated state given agent-state is ready")
    class MrdStateUpdated {
        @Test
        void returnsMrdStateReady_when_currentMrdStateIsNotReady() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.NOT_READY);
            assertEquals(Enums.AgentMrdStateName.READY, mrdStateReady.getNewState(agent, agentMrdState));
        }

        @Test
        void returnsMrdStateReady_when_currentMrdStateIsActive_zeroActivePushTasksOnMrd() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            Agent agentSpy = spy(agent);
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.ACTIVE);
            when(agentSpy.getNoOfActivePushTasks(agentMrdState.getMrd().getId())).thenReturn(0);
            assertEquals(Enums.AgentMrdStateName.READY, mrdStateReady.getNewState(agentSpy, agentMrdState));
        }

        @Test
        void returnsMrdStateReady_when_currentMrdStateIsPendingNotReady_zeroActivePushTasksOnMrd() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            Agent agentSpy = spy(agent);
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.PENDING_NOT_READY);
            when(agentSpy.getNoOfActivePushTasks(agentMrdState.getMrd().getId())).thenReturn(0);
            assertEquals(Enums.AgentMrdStateName.READY, mrdStateReady.getNewState(agentSpy, agentMrdState));
        }

        @Test
        void returnsMrdStateActive_when_currentMrdStateIsPendingNotReady_ActivePushTasksOnMrdBetweenZeroAndMax() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            Agent agentSpy = spy(agent);
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.PENDING_NOT_READY);
            when(agentSpy.getNoOfActivePushTasks(agentMrdState.getMrd().getId())).thenReturn(2);
            assertEquals(Enums.AgentMrdStateName.ACTIVE, mrdStateReady.getNewState(agentSpy, agentMrdState));
        }

        @Test
        void returnsMrdStateBusy_when_currentMrdStateIsPendingNotReady_MaxActivePushTasksOnMrd() {
            Agent agent = getNewAgent(new AgentState(Enums.AgentStateName.READY, null));
            Agent agentSpy = spy(agent);
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.PENDING_NOT_READY);
            when(agentSpy.getNoOfActivePushTasks(agentMrdState.getMrd().getId()))
                    .thenReturn(agentMrdState.getMrd().getMaxRequests());
            assertEquals(Enums.AgentMrdStateName.BUSY, mrdStateReady.getNewState(agentSpy, agentMrdState));
        }
    }

    private Agent getNewAgent(AgentState agentState) {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        Agent agent = new Agent(ccUser);
        agent.setState(agentState);
        return agent;
    }

    private MediaRoutingDomain getNewMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        mrd.setMaxRequests(5);
        return mrd;
    }
}