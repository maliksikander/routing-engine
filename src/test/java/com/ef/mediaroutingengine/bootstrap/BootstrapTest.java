package com.ef.mediaroutingengine.bootstrap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.routing.repository.AgentsRepository;
import com.ef.mediaroutingengine.routing.repository.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.routing.repository.RoutingAttributeRepository;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    @Mock
    private RestRequest restRequest;

    @BeforeEach
    void setUp() {
        bootstrap = new Bootstrap(agentsRepository, mediaRoutingDomainRepository, precisionQueueRepository,
                tasksRepository, agentPresenceRepository, routingAttributeRepository, agentsPool, mrdPool,
                precisionQueuesPool, routingAttributesPool, tasksPool, taskManager, jmsCommunicator, restRequest);
    }

    @Test
    void testSubscribeToStateEventsChannel_returnsTrue_whenSubscriptionIsSuccessful() {
        boolean isSubscribed = this.bootstrap.subscribeToStateEventsChannel();
        assertTrue(isSubscribed);
    }

    @Nested
    @DisplayName("Test getMrdFromConfigDb method")
    public class GetMrdFromConfigDb {
        @Test
        void when_allBootstrapMrdsExist() {
            List<MediaRoutingDomain> mediaRoutingDomainList = new ArrayList<>();
            mediaRoutingDomainList.add(createChatMrd());
            mediaRoutingDomainList.add(createCiscoCcMrd());
            mediaRoutingDomainList.add(createCxVoiceMrd());

            doReturn(mediaRoutingDomainList).when(mediaRoutingDomainRepository).findAll();

            bootstrap.getMrdFromConfigDb();

            verify(mediaRoutingDomainRepository, never()).save(any());
        }

        @Test
        void when_chatMrdDoesNotExist() {
            List<MediaRoutingDomain> mediaRoutingDomainList = new ArrayList<>();
            mediaRoutingDomainList.add(createCiscoCcMrd());
            mediaRoutingDomainList.add(createCxVoiceMrd());

            List<MediaRoutingDomain> passedList = new ArrayList<>();
            passedList.add(mediaRoutingDomainList.get(0));
            passedList.add(mediaRoutingDomainList.get(1));

            doReturn(passedList).when(mediaRoutingDomainRepository).findAll();

            bootstrap.getMrdFromConfigDb();

            // verify(bootstrap, times(1)).createChatMrd();
            assertNotEquals(mediaRoutingDomainList.size(),passedList.size());
            verify(mediaRoutingDomainRepository, times(1)).save(any());
        }

        @Test
        void when_ciscoCcMrdDoesNotExist() {
            List<MediaRoutingDomain> mediaRoutingDomainList = new ArrayList<>();
            mediaRoutingDomainList.add(createChatMrd());
            mediaRoutingDomainList.add(createCxVoiceMrd());

            List<MediaRoutingDomain> passedList = new ArrayList<>();
            passedList.add(mediaRoutingDomainList.get(0));
            passedList.add(mediaRoutingDomainList.get(1));

            doReturn(passedList).when(mediaRoutingDomainRepository).findAll();

            bootstrap.getMrdFromConfigDb();

            // verify(bootstrap, times(1)).createChatMrd();
            assertNotEquals(mediaRoutingDomainList.size(),passedList.size());
            verify(mediaRoutingDomainRepository, times(1)).save(any());
        }

        @Test
        void when_cxVoiceMrdDoesNotExist() {
            List<MediaRoutingDomain> mediaRoutingDomainList = new ArrayList<>();
            mediaRoutingDomainList.add(createCiscoCcMrd());
            mediaRoutingDomainList.add(createChatMrd());

            List<MediaRoutingDomain> passedList = new ArrayList<>();
            passedList.add(mediaRoutingDomainList.get(0));
            passedList.add(mediaRoutingDomainList.get(1));

            doReturn(passedList).when(mediaRoutingDomainRepository).findAll();

            bootstrap.getMrdFromConfigDb();

            // verify(bootstrap, times(1)).createChatMrd();
            assertNotEquals(mediaRoutingDomainList.size(),passedList.size());
            verify(mediaRoutingDomainRepository, times(1)).save(any());
        }
    }

    private MediaRoutingDomain createCiscoCcMrd() {
        MediaRoutingDomain ciscoCcMrd = new MediaRoutingDomain();
        ciscoCcMrd.setId(Constants.CISCO_CC_MRD_ID);
        ciscoCcMrd.setName("CISCO CC");
        ciscoCcMrd.setInterruptible(false);
        ciscoCcMrd.setDescription("Standard voice MRD for CISCO CC");
        ciscoCcMrd.setMaxRequests(1);
        ciscoCcMrd.setManagedByRe(false);
        return ciscoCcMrd;
    }

    private MediaRoutingDomain createCxVoiceMrd() {
        MediaRoutingDomain cxVoiceMrd = new MediaRoutingDomain();
        cxVoiceMrd.setId(Constants.CX_VOICE_MRD_ID);
        cxVoiceMrd.setName("CX VOICE");
        cxVoiceMrd.setInterruptible(false);
        cxVoiceMrd.setDescription("Standard voice MRD for CX Voice");
        cxVoiceMrd.setMaxRequests(1);
        cxVoiceMrd.setManagedByRe(true);
        return cxVoiceMrd;
    }

    private MediaRoutingDomain createChatMrd() {
        MediaRoutingDomain chatMrd = new MediaRoutingDomain();
        chatMrd.setId(Constants.CHAT_MRD_ID);
        chatMrd.setName("CHAT");
        chatMrd.setDescription("Standard chat MRD");
        chatMrd.setMaxRequests(5);
        chatMrd.setManagedByRe(true);
        return chatMrd;
    }


    private List<CCUser> getCcUsers(int noOfCcUsers) {
        List<CCUser> result = new ArrayList<>();
        for (int i = 0; i < noOfCcUsers; i++) {
            result.add(this.getCcUser(this.getKeyCloakUser()));
        }
        return result;
    }

    private KeycloakUser getKeyCloakUser() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID().toString());
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
