package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MrdStateBusyTest {
    MrdStateBusy mrdStateBusy = new MrdStateBusy();

    @Nested
    @DisplayName("Mrd State busy not updated")
    class MrdStateBusyNotUpdated {
        @Test
        void testGetNewState_returnsCurrentMrdState_when_currentMrdStateIs_Logout() {
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.LOGOUT);
            assertEquals(agentMrdState.getState(), mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
        }

        @Test
        void testGetNewState_returnsCurrentMrdState_when_currentMrdStateIs_Login() {
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.LOGIN);
            assertEquals(agentMrdState.getState(), mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
        }

        @Test
        void testGetNewState_returnsCurrentMrdState_when_currentMrdStateIs_NotReady() {
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Chat"),
                    Enums.AgentMrdStateName.NOT_READY);
            assertEquals(agentMrdState.getState(), mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
        }

        @Test
        void testGetNewState_returnsCurrentMrdState_when_currentMrdStateIs_PendingNotReady() {
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Chat"),
                    Enums.AgentMrdStateName.PENDING_NOT_READY);
            assertEquals(agentMrdState.getState(), mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
        }

        @Test
        void testGetNewState_returnsCurrentMrdState_when_currentMrdStateIs_Ready() {
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.READY);
            assertEquals(agentMrdState.getState(), mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
        }

        @Test
        void testGetNewState_returnsCurrentMrdState_when_currentMrdStateIs_Interrupted() {
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Chat"),
                    Enums.AgentMrdStateName.INTERRUPTED);
            assertEquals(agentMrdState.getState(), mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
        }

        @Test
        void testGetNewState_returnsCurrentMrdState_when_currentMrdStateIs_Busy() {
            AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.BUSY);
            assertEquals(agentMrdState.getState(), mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
        }
    }

    @Test
    void testGetNewState_returnsMrdState_Busy_when_currentMrdStateIs_Active() {
        AgentMrdState agentMrdState = new AgentMrdState(getNewMrd("Voice"), Enums.AgentMrdStateName.ACTIVE);
        assertEquals(Enums.AgentMrdStateName.BUSY, mrdStateBusy.getNewState(getNewAgent(), agentMrdState));
    }

    private Agent getNewAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }

    private MediaRoutingDomain getNewMrd(String name) {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName(name);
        mrd.setDescription(name + " Description");
        return mrd;
    }
}