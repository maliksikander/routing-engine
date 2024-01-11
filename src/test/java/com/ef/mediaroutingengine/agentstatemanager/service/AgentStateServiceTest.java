package com.ef.mediaroutingengine.agentstatemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.service.AgentsService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentStateServiceTest {
    @Mock
    private AgentStateListener agentStateListener;
    @Mock
    private AgentMrdStateListener agentMrdStateListener;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private AgentsService agentsService;

    private AgentStateService agentStateService;

    @BeforeEach
    void setUp() {
        this.agentStateService = new AgentStateService(agentStateListener, agentMrdStateListener, agentsPool,
                agentsService);
    }

    @Test
    void test_agentMrdState() {
        String agentId = UUID.randomUUID().toString();
        Agent agent = mock(Agent.class);
        AgentStateService spy = Mockito.spy(agentStateService);

        doReturn(agent).when(spy).validateAndGetAgent(agentId);

        AgentMrdStateChangeRequest request = new AgentMrdStateChangeRequest(agentId, UUID.randomUUID().toString(),
                Enums.AgentMrdStateName.READY);

        spy.agentMrdState(request);

        verify(agentMrdStateListener, times(1)).propertyChange(agent, request.getMrdId(),
                request.getState(), true);
        verifyNoMoreInteractions(agentMrdStateListener);
    }

    @Test
    void testAgentState_callsAgentStateListener_withRequestedAgentState() {
        String agentId = UUID.randomUUID().toString();
        AgentState agentState = new AgentState(Enums.AgentStateName.READY, null);
        AgentStateChangeRequest request = new AgentStateChangeRequest(agentId, agentState);

        Agent agent = mock(Agent.class);
        AgentStateService spy = spy(agentStateService);
        doReturn(agent).when(spy).validateAndGetAgent(agentId);

        spy.agentState(request);
        verify(agentStateListener, times(1)).propertyChange(agent, agentState, false);
    }

    @Nested
    @DisplayName("agentLogin method tests")
    class AgentLoginTests {
        @Test
        void callsAgentStateListenerWithStateLogin_whenAgentFoundInPool() {
            KeycloakUser request = getKeyCloakUserInstance();
            Agent agent = mock(Agent.class);

            when(agentsPool.findBy(request.getId())).thenReturn(agent);
            agentStateService.agentLogin(request);

            ArgumentCaptor<AgentState> captor = ArgumentCaptor.forClass(AgentState.class);
            verify(agentStateListener, times(1)).propertyChange(eq(agent), captor.capture(), eq(false));

            assertEquals(Enums.AgentStateName.LOGIN, captor.getValue().getName());
            assertNull(captor.getValue().getReasonCode());
        }

        private KeycloakUser getKeyCloakUserInstance() {
            KeycloakUser keycloakUser = new KeycloakUser();
            keycloakUser.setId(UUID.randomUUID().toString());
            keycloakUser.setUsername("user");
            return keycloakUser;
        }
    }

    @Nested
    @DisplayName("validateAndGetAgent method tests")
    class ValidateAndGetAgentTest {
        @Test
        void throwsNotFoundException_when_agentNotFoundInPool() {
            String agentId = UUID.randomUUID().toString();
            when(agentsPool.findBy(agentId)).thenReturn(null);
            assertThrows(NotFoundException.class, () -> agentStateService.validateAndGetAgent(agentId));
        }

        @Test
        void returnsAgent_when_validationSuccessful() {
            String agentId = UUID.randomUUID().toString();
            Agent agent = mock(Agent.class);

            when(agentsPool.findBy(agentId)).thenReturn(agent);

            Agent found = agentStateService.validateAndGetAgent(agentId);
            assertEquals(agent, found);
        }
    }
}