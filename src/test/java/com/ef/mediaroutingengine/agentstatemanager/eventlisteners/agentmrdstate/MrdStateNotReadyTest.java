package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.enums.MrdTypeName;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.MrdTypePool;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MrdStateNotReadyTest {
    @Mock
    private MrdTypePool mrdTypePool;

    private MrdStateNotReady mrdStateNotReady;

    @BeforeEach
    void setUp() {
        this.mrdStateNotReady = new MrdStateNotReady(this.mrdTypePool);
    }

    @Nested
    @DisplayName("getNewState method, returns current mrd-state")
    class MrdStateNotUpdated {
        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsLogout() {
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.LOGOUT);
            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsLogin() {
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.LOGIN);
            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsNotReady() {
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.NOT_READY);
            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsInterrupted() {
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.INTERRUPTED);
            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsCurrentMrdState_when_currentMrdStateIsPendingNotReady_atLeastOneActivePushTasksOnMrd() {
            Agent agentSpy = spy(getNewAgent());
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.PENDING_NOT_READY);

            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            when(agentSpy.getNoOfActiveQueueTasks(currentState.getMrd().getId())).thenReturn(1);

            assertEquals(currentState.getState(), mrdStateNotReady.getNewState(agentSpy, currentState));
        }
    }

    @Nested
    @DisplayName("getNewState method, returns updated mrd-state")
    class MrdStateUpdated {
        @Test
        void returnsMrdStateNotReady_when_currentMrdStateIsReady() {
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.READY);
            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            assertEquals(Enums.AgentMrdStateName.NOT_READY, mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsMrdStateNotReady_when_currentMrdStateIsPendingNotReady_zeroActivePushTasksOnMrd() {
            Agent agentSpy = spy(getNewAgent());
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.PENDING_NOT_READY);

            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            when(agentSpy.getNoOfActiveQueueTasks(currentState.getMrd().getId())).thenReturn(0);

            assertEquals(Enums.AgentMrdStateName.NOT_READY, mrdStateNotReady.getNewState(agentSpy, currentState));
        }

        @Test
        void returnsMrdStatePendingNotReady_when_currentMrdStateIsActive() {
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.ACTIVE);
            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
            assertEquals(Enums.AgentMrdStateName.PENDING_NOT_READY,
                    mrdStateNotReady.getNewState(getNewAgent(), currentState));
        }

        @Test
        void returnsMrdStatePendingNotReady_when_currentMrdStateIsBusy() {
            AgentMrdState currentState = createAgentMrdState(Enums.AgentMrdStateName.BUSY);
            when(mrdTypePool.getById(Constants.CHAT_MRD_TYPE_ID)).thenReturn(getMrdType());
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

    private AgentMrdState createAgentMrdState(Enums.AgentMrdStateName state) {
        MediaRoutingDomain mrd = new MediaRoutingDomain(UUID.randomUUID().toString(), Constants.CHAT_MRD_TYPE_ID, "", "", 5);
        return new AgentMrdState(mrd, state);
    }

    private MrdType getMrdType() {
        return new MrdType(Constants.CHAT_MRD_TYPE_ID, MrdTypeName.CHAT, true, true, true);
    }
}