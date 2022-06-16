package com.ef.mediaroutingengine.services.controllerservices;

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
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    void test_addMrdAsAssociatedMrdForAllAgentsInDb() {
        List<CCUser> ccUsers = new ArrayList<>();
        ccUsers.add(getCcUserInstance(UUID.randomUUID().toString()));
        when(agentsService.retrieve()).thenReturn(ccUsers);
        mediaRoutingDomainsService.addMrdAsAssociatedMrdForAllAgentsInDb(getMrdInstance(UUID.randomUUID().toString()));
        assertEquals(2, agentsService.retrieve().get(0).getAssociatedMrds().size());
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
    void test_deleteAssociatedMrdForAllAgentsFromDb() {
        CCUser agent = getCcUserInstance(UUID.randomUUID().toString());
        List<CCUser> ccUsers = new ArrayList<>();
        ccUsers.add(agent);

        when(agentsService.retrieve()).thenReturn(ccUsers);

        mediaRoutingDomainsService.deleteAssociatedMrdForAllAgentsFromDb(agent.getAssociatedMrds().get(0).getMrdId());
        assertEquals(0, agentsService.retrieve().get(0).getAssociatedMrds().size());
    }

    private List<AgentMrdState> getAgentMrdStateList(int size) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            agentMrdStates.add(new AgentMrdState(getMrdInstance(UUID.randomUUID().toString()), Enums.AgentMrdStateName.NOT_READY));
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