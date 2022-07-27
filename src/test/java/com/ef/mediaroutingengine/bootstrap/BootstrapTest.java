package com.ef.mediaroutingengine.bootstrap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.routing.repository.AgentsRepository;
import com.ef.mediaroutingengine.routing.repository.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.routing.repository.RoutingAttributeRepository;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BootstrapTest {
    private Bootstrap bootstrap;

    @Mock
    private AgentsRepository agentsRepository;
    @Mock
    private MediaRoutingDomainRepository mediaRoutingDomainRepository;
    @Mock
    private PrecisionQueueRepository precisionQueueRepository;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private AgentPresenceRepository agentPresenceRepository;
    @Mock
    private RoutingAttributeRepository routingAttributeRepository;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private MrdPool mrdPool;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private RoutingAttributesPool routingAttributesPool;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        bootstrap = new Bootstrap(agentsRepository, mediaRoutingDomainRepository, precisionQueueRepository,
                tasksRepository, agentPresenceRepository, routingAttributeRepository, agentsPool, mrdPool,
                precisionQueuesPool, routingAttributesPool, tasksPool, taskManager, jmsCommunicator);
    }

    @Test
    void testSubscribeToStateEventsChannel_returnsTrue_whenSubscriptionIsSuccessful() {
        boolean isSubscribed = this.bootstrap.subscribeToStateEventsChannel();
        assertTrue(isSubscribed);
    }

//    @Test
//    void testLoadPool_AgentsLoadedWithInitialState_whenNoAgentInAgentPresenceRepository() {
//        List<CCUser> ccUsers = this.getCcUsers(3);
//        when(this.agentsRepository.findAll()).thenReturn(ccUsers);
//        List<MediaRoutingDomain> mrdList = this.getMrdList(1);
//        when(this.mediaRoutingDomainRepository.findAll()).thenReturn(mrdList);
//    }

    private List<CCUser> getCcUsers(int noOfCcUsers) {
        List<CCUser> result = new ArrayList<>();
        for (int i = 0; i < noOfCcUsers; i++) {
            result.add(this.getCcUser(this.getKeyCloakUser()));
        }
        return result;
    }

    private KeycloakUser getKeyCloakUser() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        return keycloakUser;
    }

    private CCUser getCcUser(KeycloakUser keycloakUser) {
        CCUser ccUser = new CCUser();
        ccUser.setId(keycloakUser.getId());
        ccUser.setKeycloakUser(keycloakUser);
        return ccUser;
    }

    private List<MediaRoutingDomain> getMrdList(int noOfMrd) {
        List<MediaRoutingDomain> result = new ArrayList<>();
        for (int i = 0; i < noOfMrd; i++) {
            MediaRoutingDomain mrd = new MediaRoutingDomain();
            mrd.setId(UUID.randomUUID().toString());
            result.add(mrd);
        }
        return result;
    }
}
