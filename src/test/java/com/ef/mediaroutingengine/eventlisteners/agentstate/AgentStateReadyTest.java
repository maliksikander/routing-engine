package com.ef.mediaroutingengine.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentStateReadyTest {
    private AgentStateReady agentStateReady;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;

    @BeforeEach
    void setUp() {
        this.agentStateReady = new AgentStateReady(agentPresenceRepository);
    }

    @Test
    void testUpdateState_returnsFalse_when_currentAgentStateIsNot_NotReady() {
        AgentState newState = new AgentState(Enums.AgentStateName.READY, null);
        Agent agent = getNewAgent();

        agent.setState(new AgentState(Enums.AgentStateName.READY, null));
        assertFalse(this.agentStateReady.updateState(agent, newState));

        agent.setState(new AgentState(Enums.AgentStateName.LOGOUT, null));
        assertFalse(this.agentStateReady.updateState(agent, newState));

        agent.setState(new AgentState(Enums.AgentStateName.LOGIN, null));
        assertFalse(this.agentStateReady.updateState(agent, newState));
    }

    @Test
    void testUpdateState_updatesStateAndReturnsTrue_when_currentStateIs_NotReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.NOT_READY, null));

        AgentState newState = new AgentState(Enums.AgentStateName.READY, null);

        boolean isStateUpdated = this.agentStateReady.updateState(agent, newState);
        // Assert agent state is updated to new state
        assertEquals(newState, agent.getState());
        // verify the correct repository calls are made
        verify(this.agentPresenceRepository, times(1)).updateAgentState(any(), eq(newState));
        verifyNoMoreInteractions(this.agentPresenceRepository);
        // Assert return value is true.
        assertTrue(isStateUpdated);
    }

    private Agent getNewAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }
}