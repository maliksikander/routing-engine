package com.ef.mediaroutingengine.eventlisteners.agentstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.dto.AgentStateChangedResponse;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentStateLoginTest {
    private AgentStateLogin agentStateLogin;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private JmsCommunicator jmsCommunicator;

    @BeforeEach
    void setUp() {
        this.agentStateLogin = new AgentStateLogin(agentPresenceRepository, jmsCommunicator);
    }

    @Test
    void test_publish() throws JMSException, JsonProcessingException {
        AgentPresence agentPresence = mock(AgentPresence.class);
        ArgumentCaptor<AgentStateChangedResponse> captor = ArgumentCaptor.forClass(AgentStateChangedResponse.class);

        this.agentStateLogin.publish(agentPresence);
        //verify the jms publish call
        verify(this.jmsCommunicator, times(1))
                .publish(captor.capture(), eq(Enums.JmsEventName.AGENT_STATE_CHANGED));
        verifyNoMoreInteractions(this.jmsCommunicator);
        AgentStateChangedResponse res = captor.getValue();
        // Assert that the response object for jms publish has correct fields.
        assertEquals(agentPresence, res.getAgentPresence());
        assertTrue(res.isAgentStateChanged());
    }

    @Test
    void test_logoutToLogin() {
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.LOGOUT));
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Voice"), Enums.AgentMrdStateName.LOGOUT));

        Agent agent = getNewAgent();
        agent.setAgentMrdStates(agentMrdStateList);

        AgentState newState = new AgentState(Enums.AgentStateName.LOGIN, null);
        AgentStateLogin spy = Mockito.spy(agentStateLogin);

        AgentPresence agentPresence = mock(AgentPresence.class);
        when(agentPresenceRepository.find(agent.getId().toString())).thenReturn(agentPresence);
        // No need to test publish again, it is already tested.
        doNothing().when(spy).publish(agentPresence);
        // calling the testing method
        spy.logoutToLogin(agent, newState);
        // Assert that agent-state is updated to new state i.e. login
        assertEquals(newState, agent.getState());
        // Assert that agent-mrd-states are updated to log-in
        for (AgentMrdState agentMrdState : agent.getAgentMrdStates()) {
            assertEquals(Enums.AgentMrdStateName.LOGIN, agentMrdState.getState());
        }
        // verify agentPresence object is updated correctly
        verify(agentPresence, times(1)).setState(agent.getState());
        verify(agentPresence, times(1)).setAgentMrdStates(agent.getAgentMrdStates());
        verifyNoMoreInteractions(agentPresence);
    }

    @Test
    void test_loginToNotReady() {
        List<AgentMrdState> agentMrdStateList = new ArrayList<>();
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Chat"), Enums.AgentMrdStateName.LOGIN));
        agentMrdStateList.add(new AgentMrdState(getNewMrd("Voice"), Enums.AgentMrdStateName.LOGIN));

        Agent agent = getNewAgent();
        agent.setAgentMrdStates(agentMrdStateList);

        AgentState newState = new AgentState(Enums.AgentStateName.NOT_READY, null);
        // calling the testing method
        this.agentStateLogin.loginToNotReady(agent, newState);
        // Assert that the agent-state is updated to new state i.e. NotReady
        assertEquals(newState, agent.getState());
        // Assert that the agent-mrd-states are updated to NotReady
        for (AgentMrdState agentMrdState: agent.getAgentMrdStates()) {
            assertEquals(Enums.AgentMrdStateName.NOT_READY, agentMrdState.getState());
        }
        // verify that agentPresenceRepository calls are made correctly
        verify(agentPresenceRepository, times(1))
                .updateAgentState(agent.getId(), newState);
        verify(agentPresenceRepository, times(1))
                .updateAgentMrdStateList(agent.getId(), agent.getAgentMrdStates());
        verifyNoMoreInteractions(agentPresenceRepository);
    }

    @Test
    void testUpdateState_returnsFalse_when_currentAgentStateIsNot_Logout() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.NOT_READY, null));

        AgentState newState = new AgentState(Enums.AgentStateName.LOGIN, null);

        assertFalse(this.agentStateLogin.updateState(agent, newState));
    }

    @Test
    void testUpdateState_updatesStateAndReturnsTrue_when_currentAgentStateIs_Logout() {
        Agent agent = getNewAgent();
        agent.setState(new AgentState(Enums.AgentStateName.LOGOUT, null));

        AgentState newState = new AgentState(Enums.AgentStateName.LOGIN, null);
        AgentStateLogin spy = Mockito.spy(agentStateLogin);

        // No need to test the following methods, they are already tested
        doNothing().when(spy).logoutToLogin(agent, newState);
        doNothing().when(spy).loginToNotReady(eq(agent), any());

        assertTrue(spy.updateState(agent, newState));
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