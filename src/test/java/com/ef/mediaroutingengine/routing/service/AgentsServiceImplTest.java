package com.ef.mediaroutingengine.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AssociatedMrd;
import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.RoutingAttributeType;
import com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.repository.AgentsRepository;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class AgentsServiceImplTest {
    AgentsServiceImpl agentsService;
    @Mock
    private AgentsRepository repository;
    @Mock
    private RoutingAttributesPool routingAttributesPool;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private MrdPool mrdPool;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private AgentMrdStateListener agentMrdStateListener;

    @BeforeEach
    void setUp() {
        this.agentsService =
                new AgentsServiceImpl(repository, routingAttributesPool, agentsPool, mrdPool, precisionQueuesPool,
                        agentPresenceRepository, agentMrdStateListener);
    }


    @Test
    void test_create() {
        CCUser ccUser = getNewCcUser();

        AgentsServiceImpl spy = Mockito.spy(agentsService);

        doNothing().when(spy).validateAndSetRoutingAttributes(ccUser);
        when(this.mrdPool.findAll()).thenReturn(new ArrayList<>());

        spy.createOrUpdate(ccUser);

        ArgumentCaptor<String> agentIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(this.agentPresenceRepository, times(1))
                .save(agentIdCaptor.capture(), any());
        assertEquals(ccUser.getKeycloakUser().getId(), agentIdCaptor.getValue());

        verify(this.precisionQueuesPool, times(1)).evaluateOnInsertForAll(any());
        verify(this.agentsPool, times(1)).insert(any());
        verify(this.repository, times(1)).insert(ccUser);
    }

    @Nested
    @DisplayName("update method tests")
    class UpdateTest {
        @Test
        void throwsNotFoundException_when_agentDoesNotExistInRepository() {
            CCUser ccUser = getNewCcUser();
            String id = ccUser.getKeycloakUser().getId();
            when(repository.existsById(id)).thenReturn(false);

            assertThrows(NotFoundException.class, () -> agentsService.update(ccUser, id));
        }

        @Test
        void when_updateSuccessful() {
            CCUser ccUser = getNewCcUser();
            String mrdId = ccUser.getAssociatedMrds().get(0).getMrdId();
            String id = ccUser.getKeycloakUser().getId();

            AgentsServiceImpl spy = Mockito.spy(agentsService);

            when(repository.existsById(id)).thenReturn(true);
            doNothing().when(spy).validateAndSetRoutingAttributes(ccUser);

            Agent agent = mock(Agent.class);
            when(agentsPool.findBy(id)).thenReturn(agent);
            when(agent.getAgentMrdState(mrdId)).thenReturn(getAgentMrdState(Enums.AgentMrdStateName.BUSY));

            spy.update(ccUser, id);

            ArgumentCaptor<CCUser> ccUserCaptor = ArgumentCaptor.forClass(CCUser.class);
            verify(agent, times(1)).updateFrom(ccUserCaptor.capture());
            assertEquals(id, ccUserCaptor.getValue().getId());

            verify(agentPresenceRepository, times(1)).updateCcUser(ccUser);
            verify(precisionQueuesPool, times(1)).evaluateOnUpdateForAll(agent);
            verify(repository, times(1)).save(ccUser);
        }
    }

    @Nested
    @DisplayName("delete method tests")
    class DeleteTest {
        @Test
        void throwsNotFoundException_when_agentDoesNotExistInRepository() {
            String id = UUID.randomUUID().toString();
            when(repository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> agentsService.delete(id));
        }

        @Test
        void returnsConflictResponse_when_agentHasTasks() {
            String id = UUID.randomUUID().toString();
            Agent agent = mock(Agent.class);

            List<Task> taskList = new ArrayList<>();
            taskList.add(mock(Task.class));

            Optional<CCUser> optionalCCUser = Optional.of(getNewCcUser());

            when(repository.findById(id)).thenReturn(optionalCCUser);
            when(agentsPool.findBy(id)).thenReturn(agent);
            when(agent.getAllTasks()).thenReturn(taskList);

            ResponseEntity<Object> response = agentsService.delete(id);
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void deletesAgentAndReturnsOkResponse_when_deleteSuccessful() {
            String id = UUID.randomUUID().toString();
            Agent agent = mock(Agent.class);

            Optional<CCUser> optionalCCUser = Optional.of(getNewCcUser());

            when(repository.findById(id)).thenReturn(optionalCCUser);
            when(agentsPool.findBy(id)).thenReturn(agent);
            when(agent.getAllTasks()).thenReturn(new ArrayList<>());

            ResponseEntity<Object> response = agentsService.delete(id);

            CCUser ccUser = optionalCCUser.get();

            verify(agent, times(1)).updateFrom(ccUser);
            verify(agentPresenceRepository, times(1)).updateCcUser(ccUser);
            verify(precisionQueuesPool, times(1)).deleteFromAll(agent);
            verify(repository, times(1)).save(ccUser);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }


    @Nested
    @DisplayName("validateAndSetRoutingAttributes method tests")
    class ValidateAndSetRoutingAttributesTest {
        @Test
        void when_validationSuccessful() {
            CCUser ccUser = getNewCcUser();

            for (int i = 0; i < ccUser.getAssociatedRoutingAttributes().size(); i++) {
                when(routingAttributesPool.findById(any())).thenReturn(mock(RoutingAttribute.class));
            }
            agentsService.validateAndSetRoutingAttributes(ccUser);

            verifyNoMoreInteractions(routingAttributesPool);
        }

        @Test
        void throwsNotFoundException_when_attributeNotFoundInPool() {
            CCUser ccUser = getNewCcUser();
            when(routingAttributesPool.findById(any())).thenReturn(null);
            assertThrows(NotFoundException.class, () -> agentsService.validateAndSetRoutingAttributes(ccUser));
        }

        @Test
        void doesNothing_when_associatedAttributesIsNull() {
            CCUser ccUser = getNewCcUser();
            ccUser.setAssociatedRoutingAttributes(null);

            agentsService.validateAndSetRoutingAttributes(ccUser);
            verifyNoInteractions(routingAttributesPool);
        }
    }

    @Test
    void test_saveUpdatedAgentInDb(CapturedOutput output) {
        CCUser agent = getNewCcUser();
        agentsService.saveUpdatedAgentInDb(agent);
        verify(repository, times(1)).save(agent);
        Assertions.assertThat(output).contains("Agent updated in Agents config DB");
    }

    @Test
    void test_setAssociatedMrdsAndMaxAgentTasks() {
        CCUser agent = getNewCcUser();
        List<AgentMrdState> agentMrdStates = List.of(getAgentMrdState(Enums.AgentMrdStateName.ACTIVE));
        agentsService.setAssociatedMrdsAndMaxAgentTasks(agent, agentMrdStates);
        assertEquals(2, agent.getAssociatedMrds().size());
    }

    @Test
    void test_updateAgentMrdStateToBusy_whenActivePushTaskGreaterOrEqualToMrdMaxRequests(CapturedOutput output) {
        CCUser ccUser = getNewCcUser();
        String mrdId = ccUser.getAssociatedMrds().get(0).getMrdId();
        Agent agent = mock(Agent.class);

        when(agentsPool.findBy(ccUser.getId())).thenReturn(agent);
        when(agent.getNoOfActiveQueueTasks(mrdId)).thenReturn(12);
        when(agent.getAgentMrdState(mrdId)).thenReturn(getAgentMrdState(Enums.AgentMrdStateName.ACTIVE));

        agentsService.updateAgentMrdState(ccUser);
        Assertions.assertThat(output).contains("MRD state has been changed from ACTIVE to BUSY.");
    }

    @Test
    void test_updateAgentMrdStateToActive_whenActivePushTaskLessThanMrdMaxRequests(CapturedOutput output) {
        CCUser ccUser = getNewCcUser();
        String mrdId = ccUser.getAssociatedMrds().get(0).getMrdId();
        Agent agent = mock(Agent.class);

        when(agentsPool.findBy(ccUser.getId())).thenReturn(agent);
        when(agent.getNoOfActiveQueueTasks(mrdId)).thenReturn(3);
        when(agent.getAgentMrdState(mrdId)).thenReturn(getAgentMrdState(Enums.AgentMrdStateName.BUSY));

        agentsService.updateAgentMrdState(ccUser);
        Assertions.assertThat(output).contains("MRD state has been changed from BUSY to ACTIVE.");

    }


    private CCUser getNewCcUser() {
        List<AssociatedRoutingAttribute> attributes = new ArrayList<>();
        attributes.add(getNewAssociatedAttribute("Sales", RoutingAttributeType.BOOLEAN, 1));
        attributes.add(getNewAssociatedAttribute("English", RoutingAttributeType.PROFICIENCY_LEVEL, 7));
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(getNewKeyClockUser());
        ccUser.setAssociatedRoutingAttributes(attributes);
        ccUser.setId(ccUser.getKeycloakUser().getId());

        AssociatedMrd associatedMrd = new AssociatedMrd(UUID.randomUUID().toString(), 5);
        ccUser.addAssociatedMrd(associatedMrd);
        return ccUser;
    }

    private KeycloakUser getNewKeyClockUser() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
        return keycloakUser;
    }

    private RoutingAttribute getNewAttribute(String name, RoutingAttributeType type) {
        RoutingAttribute routingAttribute = new RoutingAttribute();
        routingAttribute.setId(UUID.randomUUID().toString());
        routingAttribute.setName(name);
        routingAttribute.setDescription(name + "desc");
        routingAttribute.setType(type);
        routingAttribute.setDefaultValue(1);
        return routingAttribute;
    }

    private AssociatedRoutingAttribute getNewAssociatedAttribute(String name, RoutingAttributeType type, int value) {
        AssociatedRoutingAttribute associatedRoutingAttribute = new AssociatedRoutingAttribute();
        associatedRoutingAttribute.setRoutingAttribute(getNewAttribute(name, type));
        associatedRoutingAttribute.setValue(value);
        return associatedRoutingAttribute;
    }

    private AgentMrdState getAgentMrdState(Enums.AgentMrdStateName state) {
        AgentMrdState agentMrdState = new AgentMrdState();
        MediaRoutingDomain mediaRoutingDomain = new MediaRoutingDomainsServiceImplTest().getMrdInstance(UUID.randomUUID().toString());
        agentMrdState.setMrd(mediaRoutingDomain);
        agentMrdState.setState(state);
        agentMrdState.setMaxAgentTasks(10);
        return agentMrdState;
    }
}