package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
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
class MrdStateNotReadyTest {
    MrdStateNotReady mrdStateNotReady = new MrdStateNotReady();

    @Nested
    @DisplayName("getNewState method, returns current mrd-state")
    class MrdStateNotUpdated {
        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsLogout() {
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.LOGOUT);
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsLogin() {
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.LOGIN);
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsNotReady() {
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.NOT_READY);
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsInterrupted() {
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.INTERRUPTED);
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsPendingNotReady_atLeastOneActivePushTasksOnMrd() {
            Agent agentSpy = spy(getNewAgent());
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.PENDING_NOT_READY);

            when(agentSpy.getNoOfActiveQueueTasks(currentState.getMrd().getId())).thenReturn(1);
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(agentSpy, currentState));
        }
    }

    @Nested
    @DisplayName("getNewState method, returns updated mrd-state")
    class MrdStateUpdated {
        @Test
        void returnsMrdStateNotReady_when_currentMrdStateIsReady() {
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.READY);
            assertEquals(Enums.AgentMrdStateName.NOT_READY, mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsMrdStateNotReady_when_currentMrdStateIsPendingNotReady_zeroActivePushTasksOnMrd() {
            Agent agentSpy = spy(getNewAgent());
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.PENDING_NOT_READY);

            when(agentSpy.getNoOfActiveQueueTasks(currentState.getMrd().getId())).thenReturn(0);
            assertEquals(Enums.AgentMrdStateName.NOT_READY, mrdStateNotReady.getNewState(agentSpy, currentState));
        }

        @Test
        void returnsMrdStatePendingNotReady_when_currentMrdStateIsActive() {
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.ACTIVE);
            assertEquals(Enums.AgentMrdStateName.PENDING_NOT_READY,
                    mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsMrdStatePendingNotReady_when_currentMrdStateIsBusy() {
            AgentMrdState currentState = new AgentMrdState(getNewMrd(), Enums.AgentMrdStateName.BUSY);
            assertEquals(Enums.AgentMrdStateName.PENDING_NOT_READY,
                    mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }
    }

    private Agent getNewAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }

    private MediaRoutingDomain getNewMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        return mrd;
    }
}