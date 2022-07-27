package com.ef.mediaroutingengine.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AssociatedMrd;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.repository.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class MediaRoutingDomainsServiceImplTest {
    @Mock
    private MediaRoutingDomainRepository repository;
    @Mock
    private PrecisionQueueRepository precisionQueueRepository;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private MrdPool mrdPool;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private TasksRepository tasksRepository;

    private MediaRoutingDomainsServiceImpl mediaRoutingDomainsService;

    @Mock
    private AgentsServiceImpl agentsService;

    @BeforeEach
    void setUp() {
        this.mediaRoutingDomainsService = new MediaRoutingDomainsServiceImpl(repository, precisionQueueRepository,
                tasksPool, mrdPool, agentsPool, agentPresenceRepository, tasksRepository, agentsService);
    }

    @Nested
    @DisplayName("delete method tests")
    class DeleteTest {

    }

    @Test
    void test_deleteAgentMrdStateFromAllAgentPresence() {
        List<AgentPresence> agentPresenceList = new ArrayList<>();
        agentPresenceList.add(getAgentPresenceInstance(getAgentMrdStateList(2)));
        agentPresenceList.add(getAgentPresenceInstance(getAgentMrdStateList(3)));

        MediaRoutingDomainsServiceImpl spy = Mockito.spy(mediaRoutingDomainsService);

        when(agentPresenceRepository.findAll()).thenReturn(agentPresenceList);
        doNothing().when(spy).deleteAgentMrdStateFromAgentPresence(any(), any());

        spy.deleteAgentMrdStateFromAllAgentPresence("mrd1");

        verify(agentPresenceRepository, times(1)).saveAllByKeyValueMap(anyMap());
    }

    @Nested
    @DisplayName("deleteAgentMrdStateFromAgentPresence method test")
    class DeleteAgentMrdStateFromAgentPresenceTest {
        @Test
        void when_agentMrdStateFound() {
            MediaRoutingDomain mrd = getMrdInstance(UUID.randomUUID().toString());
            List<AgentMrdState> agentMrdStateList = new ArrayList<>();
            agentMrdStateList.add(new AgentMrdState(mrd, Enums.AgentMrdStateName.NOT_READY));

            AgentPresence agentPresence = getAgentPresenceInstance(agentMrdStateList);

            mediaRoutingDomainsService.deleteAgentMrdStateFromAgentPresence(mrd.getId(), agentPresence);

            assertEquals(0, agentPresence.getAgentMrdStates().size());
        }

        @Test
        void when_agentMrdStateNotFound() {
            List<AgentMrdState> agentMrdStateList = new ArrayList<>();
            agentMrdStateList.add(
                    new AgentMrdState(getMrdInstance(UUID.randomUUID().toString()), Enums.AgentMrdStateName.NOT_READY));

            AgentPresence agentPresence = getAgentPresenceInstance(agentMrdStateList);

            mediaRoutingDomainsService.deleteAgentMrdStateFromAgentPresence("mrd-1", agentPresence);
            assertEquals(1, agentPresence.getAgentMrdStates().size());
        }
    }

    @Test
    void test_updatePrecisionQueues() {
        MediaRoutingDomain mrd = getMrdInstance(UUID.randomUUID().toString());
        List<PrecisionQueueEntity> precisionQueueEntityList = new ArrayList<>();
        precisionQueueEntityList.add(getPrecisionQueueEntityInstance());

        when(precisionQueueRepository.findByMrdId(mrd.getId())).thenReturn(precisionQueueEntityList);

        mediaRoutingDomainsService.updatePrecisionQueues(mrd, mrd.getId());

        assertEquals(mrd, precisionQueueEntityList.get(0).getMrd());
        verify(precisionQueueRepository, times(1)).saveAll(precisionQueueEntityList);
    }

    @Test
    void test_whenMaxAgentTasksGreaterThanMrdMaxRequestValue_ReturnTrue() {
        boolean result = mediaRoutingDomainsService.isMaxAgentTasksGreaterThanMrdMaxRequestValue(
                UUID.randomUUID(), 7, 5);
        assertEquals(true, result);
    }

    @Test
    void test_whenMaxAgentTasksLessThanMrdMaxRequestValue_ReturnFalse() {
        boolean result = mediaRoutingDomainsService.isMaxAgentTasksGreaterThanMrdMaxRequestValue(
                UUID.randomUUID(), 12, 15);
        assertEquals(false, result);
    }

    @Test
    void test_addMrdAsAssociatedMrdForAllAgentsInDb(CapturedOutput output) {
        String mrdId = UUID.randomUUID().toString();
        Agent agent = new Agent(getCcUserInstance(mrdId));
        AgentMrdState agentMrdState = new AgentMrdState(getMrdInstance(mrdId), Enums.AgentMrdStateName.NOT_READY);
        agent.addAgentMrdState(agentMrdState);

        List<Agent> agentList = new ArrayList<>();
        agentList.add(agent);
        when(agentsPool.findAll()).thenReturn(agentList);

        mediaRoutingDomainsService.updateAllAgentsInDb();
        Assertions.assertThat(output).contains("All Agents has been updated in DB.");
    }

    @Test
    void test_getAgentsWithConflictedMaxTasks_returnConflictedAgentsList() {
        String mrdId = UUID.randomUUID().toString();
        int mrdMaxRequest = 3;

        List<MediaRoutingDomain> mediaRoutingDomains = new ArrayList<>();
        mediaRoutingDomains.add(getMrdInstance(mrdId));
        when(mrdPool.findAll()).thenReturn(mediaRoutingDomains);

        CCUser ccUser = getCcUserInstance(mrdId);
        Agent agent = new Agent(ccUser, mrdPool.findAll());
        List<Agent> agentList = new ArrayList<>();
        agentList.add(agent);

        when(agentsPool.findAll()).thenReturn(agentList);

        List<CCUser> conflictedAgents =
                mediaRoutingDomainsService.getAgentsWithConflictedMaxTasks(ccUser.getAssociatedMrds().get(0).getMrdId(),
                        mrdMaxRequest);

        assertTrue(conflictedAgents.size() != 0);
        assertEquals(agent.getId(), conflictedAgents.get(0).getId());
    }

    @Test
    void test_deleteAssociatedMrdForAllAgentsFromDb(CapturedOutput output) {
        String mrdId = UUID.randomUUID().toString();
        Agent agent = new Agent(getCcUserInstance(mrdId));
        agent.deleteAgentMrdState(mrdId);

        List<Agent> agentList = new ArrayList<>();
        agentList.add(agent);
        when(agentsPool.findAll()).thenReturn(agentList);

        mediaRoutingDomainsService.updateAllAgentsInDb();
        assertEquals(0, agentsPool.findAll().get(0).getAgentMrdStates().size());

        Assertions.assertThat(output).contains("All Agents has been updated in DB.");
    }

    private List<AgentMrdState> getAgentMrdStateList(int size) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            agentMrdStates.add(
                    new AgentMrdState(getMrdInstance(UUID.randomUUID().toString()), Enums.AgentMrdStateName.NOT_READY));
        }
        return agentMrdStates;
    }

    private AgentPresence getAgentPresenceInstance(List<AgentMrdState> agentMrdStates) {
        AgentPresence agentPresence = new AgentPresence();
        agentPresence.setAgentMrdStates(agentMrdStates);
        agentPresence.setAgent(getCcUserInstance(UUID.randomUUID().toString()));
        return agentPresence;
    }

    private CCUser getCcUserInstance(String associatedMrdId) {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());

        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.setId(keycloakUser.getId());

        AssociatedMrd associatedMrd = new AssociatedMrd(associatedMrdId, 5);
        ccUser.addAssociatedMrd(associatedMrd);
        return ccUser;
    }

    protected MediaRoutingDomain getMrdInstance(String mrdId) {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(mrdId);
        mrd.setName("MRD");
        mrd.setDescription("MRD Desc");
        mrd.setMaxRequests(5);
        return mrd;
    }

    private PrecisionQueueEntity getPrecisionQueueEntityInstance() {
        PrecisionQueueEntity entity = new PrecisionQueueEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setName("PQ");
        return entity;
    }
}