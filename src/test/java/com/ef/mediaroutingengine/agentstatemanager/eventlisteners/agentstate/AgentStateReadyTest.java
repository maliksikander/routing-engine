package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
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
        assertFalse(this.agentStateReady.updateState(agent, newState,false).isAgentStateChanged());

        agent.setState(new AgentState(Enums.AgentStateName.LOGOUT, null));
        assertFalse(this.agentStateReady.updateState(agent, newState,false).isAgentStateChanged());

        agent.setState(new AgentState(Enums.AgentStateName.LOGIN, null));
        assertFalse(this.agentStateReady.updateState(agent, newState,false).isAgentStateChanged());
    }

    @Test
    void testUpdateState_updatesStateAndReturnsTrue_when_currentStateIs_NotReady() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.NOT_READY, null));

        AgentState newState = new AgentState(Enums.AgentStateName.READY, null);

        AgentStateChangedResponse res = this.agentStateReady.updateState(agent, newState,false);
        // Assert agent state is updated to new state
        assertEquals(newState, agent.getState());
        // verify the correct repository calls are made
        verify(this.agentPresenceRepository, times(1)).updateAgentState(any(), eq(newState));
        verifyNoMoreInteractions(this.agentPresenceRepository);
        // Assert return value is true.
        assertTrue(res.isAgentStateChanged());
    }

    private Agent getNewAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }
}