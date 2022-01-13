package com.ef.mediaroutingengine.services.controllerservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
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

    @BeforeEach
    void setUp() {
        this.mediaRoutingDomainsService = new MediaRoutingDomainsServiceImpl(repository, precisionQueueRepository,
                tasksPool, mrdPool, agentsPool, agentPresenceRepository, tasksRepository);
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
            MediaRoutingDomain mrd = getMrdInstance();
            List<AgentMrdState> agentMrdStateList = new ArrayList<>();
            agentMrdStateList.add(new AgentMrdState(mrd, Enums.AgentMrdStateName.NOT_READY));

            AgentPresence agentPresence = getAgentPresenceInstance(agentMrdStateList);

            mediaRoutingDomainsService.deleteAgentMrdStateFromAgentPresence(mrd.getId(), agentPresence);

            assertEquals(0, agentPresence.getAgentMrdStates().size());
        }

        @Test
        void when_agentMrdStateNotFound() {
            List<AgentMrdState> agentMrdStateList = new ArrayList<>();
            agentMrdStateList.add(new AgentMrdState(getMrdInstance(), Enums.AgentMrdStateName.NOT_READY));

            AgentPresence agentPresence = getAgentPresenceInstance(agentMrdStateList);

            mediaRoutingDomainsService.deleteAgentMrdStateFromAgentPresence("mrd-1", agentPresence);

            assertEquals(1, agentPresence.getAgentMrdStates().size());
        }
    }


    @Test
    void test_updatePrecisionQueues() {
        MediaRoutingDomain mrd = getMrdInstance();
        List<PrecisionQueueEntity> precisionQueueEntityList = new ArrayList<>();
        precisionQueueEntityList.add(getPrecisionQueueEntityInstance());

        when(precisionQueueRepository.findByMrdId(mrd.getId())).thenReturn(precisionQueueEntityList);

        mediaRoutingDomainsService.updatePrecisionQueues(mrd, mrd.getId());

        assertEquals(mrd, precisionQueueEntityList.get(0).getMrd());
        verify(precisionQueueRepository, times(1)).saveAll(precisionQueueEntityList);
    }

    private List<AgentMrdState> getAgentMrdStateList(int size) {
        List<AgentMrdState> agentMrdStates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            agentMrdStates.add(new AgentMrdState(getMrdInstance(), Enums.AgentMrdStateName.NOT_READY));
        }
        return agentMrdStates;
    }

    private AgentPresence getAgentPresenceInstance(List<AgentMrdState> agentMrdStates) {
        AgentPresence agentPresence = new AgentPresence();
        agentPresence.setAgentMrdStates(agentMrdStates);
        agentPresence.setAgent(getCcUserInstance());
        return agentPresence;
    }

    private CCUser getCcUserInstance() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());

        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.setId(keycloakUser.getId());
        return ccUser;
    }

    private MediaRoutingDomain getMrdInstance() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
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