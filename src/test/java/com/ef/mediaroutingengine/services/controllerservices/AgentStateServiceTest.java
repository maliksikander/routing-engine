package com.ef.mediaroutingengine.services.controllerservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentLoginRequest;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentState;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
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

    private AgentStateService agentStateService;

    @BeforeEach
    void setUp() {
        this.agentStateService = new AgentStateService(agentStateListener, agentMrdStateListener, agentsPool);
    }

    @Test
    void test_agentMrdState() {
        UUID agentId = UUID.randomUUID();
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

    @Nested
    @DisplayName("agentState methods tests")
    class AgentStateTest {
        @Test
        void with_arguments_agentId_and_agentState() {
            UUID agentId = UUID.randomUUID();
            AgentState agentState = new AgentState(Enums.AgentStateName.READY, null);

            Agent agent = mock(Agent.class);
            AgentStateService spy = spy(agentStateService);

            doReturn(agent).when(spy).validateAndGetAgent(agentId);

            spy.agentState(agentId, agentState);

            verify(agentStateListener, times(1)).propertyChange(agent, agentState);
            verifyNoMoreInteractions(agentStateListener);
        }

        @Test
        void with_argument_agentStateChangeRequest() {
            UUID agentId = UUID.randomUUID();
            AgentState agentState = new AgentState(Enums.AgentStateName.READY, null);
            AgentStateChangeRequest request = new AgentStateChangeRequest(agentId, agentState);

            AgentStateService spy = spy(agentStateService);
            doNothing().when(spy).agentState(any(), any());

            spy.agentState(request);
            verify(spy, times(1)).agentState(agentId, agentState);
        }
    }

    @Test
    void test_agentLogin() {
        UUID agentId = UUID.randomUUID();
        AgentLoginRequest request = new AgentLoginRequest();
        request.setAgentId(agentId);

        AgentStateService spy = spy(agentStateService);
        doNothing().when(spy).agentState(any(), any());

        spy.agentLogin(request);

        ArgumentCaptor<AgentState> captor = ArgumentCaptor.forClass(AgentState.class);
        verify(spy, times(1)).agentState(eq(agentId), captor.capture());

        assertEquals(Enums.AgentStateName.LOGIN, captor.getValue().getName());
        assertNull(captor.getValue().getReasonCode());
    }

    @Nested
    @DisplayName("validateAndGetAgent method tests")
    class ValidateAndGetAgentTest {
        @Test
        void throwsNotFoundException_when_agentNotFoundInPool() {
            UUID agentId = UUID.randomUUID();
            when(agentsPool.findById(agentId)).thenReturn(null);
            assertThrows(NotFoundException.class, () -> agentStateService.validateAndGetAgent(agentId));
        }

        @Test
        void returnsAgent_when_validationSuccessful() {
            UUID agentId = UUID.randomUUID();
            Agent agent = mock(Agent.class);

            when(agentsPool.findById(agentId)).thenReturn(agent);

            Agent found = agentStateService.validateAndGetAgent(agentId);
            assertEquals(agent, found);
        }
    }

}