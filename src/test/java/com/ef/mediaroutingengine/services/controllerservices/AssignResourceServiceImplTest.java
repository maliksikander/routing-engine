package com.ef.mediaroutingengine.services.controllerservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Channel;
import com.ef.cim.objectmodel.ChannelConfig;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.ChannelType;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.RoutingPolicy;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
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
class AssignResourceServiceImplTest {
    @Mock
    private TaskManager taskManager;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private MrdPool mrdPool;

    private AssignResourceServiceImpl assignResourceService;

    @BeforeEach
    void setUp() {
        this.assignResourceService = new AssignResourceServiceImpl(taskManager, precisionQueuesPool, mrdPool);
    }

    @Test
    void test_assign() {
        ChannelSession channelSession = getChannelSessionInstance();
        String requestedQueue = UUID.randomUUID().toString();
        AssignResourceRequest request = new AssignResourceRequest(channelSession, requestedQueue);

        String mrdId = channelSession.getChannel().getChannelType().getMediaRoutingDomain();
        MediaRoutingDomain mrd = mock(MediaRoutingDomain.class);
        PrecisionQueue queue = mock(PrecisionQueue.class);

        AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);

        doNothing().when(spy).validateChannelSession(channelSession);
        doReturn(mrd).when(spy).validateAndGetMrd(channelSession);
        when(mrd.getId()).thenReturn(mrdId);
        doReturn(queue).when(spy).validateAndGetQueue(channelSession, requestedQueue, mrdId);

        String response = spy.assign(request);
        assertEquals("The request is received Successfully", response);
    }

    @Nested
    @DisplayName("validateChannelSession method tests")
    class ValidateChannelSessionTest {
        @Test
        void throwsIllegalArgumentException_when_channelSessionIsNull() {
            assertThrows(IllegalArgumentException.class, () -> assignResourceService.validateChannelSession(null));
        }

        @Test
        void throwsIllegalArgumentException_when_channelIsNull() {
            ChannelSession channelSession = new ChannelSession();
            channelSession.setChannel(null);
            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateChannelSession(channelSession));
        }

        @Test
        void throwsIllegalArgumentException_when_channelConnectorIsNull() {
            ChannelSession channelSession = new ChannelSession();
            channelSession.getChannel().setChannelConnector(null);
            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateChannelSession(channelSession));
        }

        @Test
        void throwsIllegalArgumentException_when_channelTypeIsNull() {
            ChannelSession channelSession = new ChannelSession();
            channelSession.getChannel().setChannelType(null);
            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateChannelSession(channelSession));
        }

        @Test
        void throwsIllegalArgumentException_when_channelConfigIsNull() {
            ChannelSession channelSession = new ChannelSession();
            channelSession.getChannel().setChannelConfig(null);
            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateChannelSession(channelSession));
        }

        @Test
        void throwsIllegalArgumentException_when_routingPolicyIsNull() {
            ChannelSession channelSession = new ChannelSession();
            channelSession.getChannel().getChannelConfig().setRoutingPolicy(null);
            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateChannelSession(channelSession));
        }

        @Test
        void throwsIllegalArgumentException_when_routingModeIsNotPush() {
            ChannelSession channelSession = getChannelSessionInstance();
            channelSession.getChannel().getChannelConfig().getRoutingPolicy().setRoutingMode(RoutingMode.PULL);
            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateChannelSession(channelSession));
        }
    }

    @Nested
    @DisplayName("validateAndGetMrd method tests")
    class ValidateAndGetMrdTest {
        @Test
        void throwsIllegalArgumentException_when_mrdNotFound() {
            ChannelSession channelSession = getChannelSessionInstance();
            when(mrdPool.findById(any())).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () -> assignResourceService.validateAndGetMrd(channelSession));
        }

        @Test
        void returnsMrd_when_validationSuccessful() {
            ChannelSession channelSession = getChannelSessionInstance();
            MediaRoutingDomain mrd = mock(MediaRoutingDomain.class);

            when(mrdPool.findById(any())).thenReturn(mrd);

            MediaRoutingDomain result = assignResourceService.validateAndGetMrd(channelSession);
            assertEquals(mrd, result);
        }
    }

    @Nested
    @DisplayName("validateAndGetQueue method tests")
    class ValidateAndGetQueueTest {
        @Test
        void throwsIllegalArgumentsException_when_defaultQueueIsNullAndRequestedQueueIdNull() {
            ChannelSession channelSession = getChannelSessionInstance();
            channelSession.getChannel().getChannelConfig().getRoutingPolicy().setRoutingObjectId(null);
            String mrdId = UUID.randomUUID().toString();

            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateAndGetQueue(channelSession, null, mrdId));
        }

        @Test
        void throwsIllegalArgumentException_when_noQueueFoundFromPool() {
            ChannelSession channelSession = getChannelSessionInstance();
            String mrdId = UUID.randomUUID().toString();

            AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);
            doReturn(null).when(spy).getPrecisionQueueFrom(any(), any());

            assertThrows(IllegalArgumentException.class,
                    () -> spy.validateAndGetQueue(channelSession, null, mrdId));
        }

        @Test
        void throwsIllegalArgumentException_when_requestMrdId_notEqualTo_mrdIdInQueueFoundFromPool() {
            ChannelSession channelSession = getChannelSessionInstance();
            String mrdId = UUID.randomUUID().toString();
            String requestedQueue = UUID.randomUUID().toString();
            AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);

            PrecisionQueue queueFound = mock(PrecisionQueue.class);
            doReturn(queueFound).when(spy).getPrecisionQueueFrom(any(), any());

            MediaRoutingDomain mrdFoundInQueue = new MediaRoutingDomain();
            mrdFoundInQueue.setId(UUID.randomUUID().toString());

            when(queueFound.getMrd()).thenReturn(mrdFoundInQueue);

            assertThrows(IllegalArgumentException.class,
                    () -> spy.validateAndGetQueue(channelSession, requestedQueue, mrdId));
        }

        @Test
        void throwsIllegalStateException_when_noStepsInQueueFoundFromPool() {
            ChannelSession channelSession = getChannelSessionInstance();
            String mrdId = UUID.randomUUID().toString();
            String requestedQueue = UUID.randomUUID().toString();
            AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);

            PrecisionQueue queueFound = mock(PrecisionQueue.class);
            doReturn(queueFound).when(spy).getPrecisionQueueFrom(any(), any());

            MediaRoutingDomain mrdFoundInQueue = new MediaRoutingDomain();
            mrdFoundInQueue.setId(mrdId);

            when(queueFound.getMrd()).thenReturn(mrdFoundInQueue);
            when(queueFound.getSteps()).thenReturn(new ArrayList<>());

            assertThrows(IllegalStateException.class,
                    () -> spy.validateAndGetQueue(channelSession, requestedQueue, mrdId));
        }

        @Test
        void returnsQueue_when_validationSuccessful() {
            ChannelSession channelSession = getChannelSessionInstance();
            String mrdId = UUID.randomUUID().toString();
            String requestedQueue = UUID.randomUUID().toString();
            AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);

            PrecisionQueue queueFound = mock(PrecisionQueue.class);
            doReturn(queueFound).when(spy).getPrecisionQueueFrom(any(), any());

            MediaRoutingDomain mrdFoundInQueue = new MediaRoutingDomain();
            mrdFoundInQueue.setId(mrdId);
            List<Step> stepList = new ArrayList<>();
            stepList.add(mock(Step.class));

            when(queueFound.getMrd()).thenReturn(mrdFoundInQueue);
            when(queueFound.getSteps()).thenReturn(stepList);

            PrecisionQueue result = spy.validateAndGetQueue(channelSession, requestedQueue, mrdId);
            assertEquals(queueFound, result);
        }
    }

    @Nested
    @DisplayName("getPrecisionQueueFrom method tests")
    class GetPrecisionQueuesFromTest {
        @Test
        void returnsRequestedQueue_when_requestedQueueFound() {
            String requestedQueueId = UUID.randomUUID().toString();
            String defaultQueueId = UUID.randomUUID().toString();

            PrecisionQueue requestedQueue = mock(PrecisionQueue.class);
            when(precisionQueuesPool.findById(requestedQueueId)).thenReturn(requestedQueue);

            PrecisionQueue found = assignResourceService.getPrecisionQueueFrom(requestedQueueId, defaultQueueId);

            assertEquals(requestedQueue, found);
        }

        @Test
        void returnsDefaultQueue_when_requestedQueueNotFound() {
            String requestedQueueId = UUID.randomUUID().toString();
            String defaultQueueId = UUID.randomUUID().toString();

            PrecisionQueue defaultQueue = mock(PrecisionQueue.class);
            when(precisionQueuesPool.findById(requestedQueueId)).thenReturn(null);
            when(precisionQueuesPool.findById(defaultQueueId)).thenReturn(defaultQueue);

            PrecisionQueue found = assignResourceService.getPrecisionQueueFrom(requestedQueueId, defaultQueueId);

            assertEquals(defaultQueue, found);
        }

        @Test
        void returnsNull_when_requestQueueAndDefaultQueueBothNotFound() {
            String requestedQueueId = UUID.randomUUID().toString();
            String defaultQueueId = UUID.randomUUID().toString();

            when(precisionQueuesPool.findById(requestedQueueId)).thenReturn(null);
            when(precisionQueuesPool.findById(defaultQueueId)).thenReturn(null);

            PrecisionQueue found = assignResourceService.getPrecisionQueueFrom(requestedQueueId, defaultQueueId);

            assertNull(found);
        }
    }

    private ChannelSession getChannelSessionInstance() {
        Channel channel = getChannelInstance();
        ChannelSession channelSession = new ChannelSession();
        channelSession.setChannel(channel);
        channelSession.setTopicId(UUID.randomUUID());
        return channelSession;
    }

    private Channel getChannelInstance() {
        ChannelConfig channelConfig = getChannelConfigInstance();
        ChannelType channelType = getChannelTypeInstance();

        Channel channel = new Channel();
        channel.setChannelConfig(channelConfig);
        channel.setChannelType(channelType);
        return channel;
    }

    private ChannelType getChannelTypeInstance() {
        ChannelType channelType = new ChannelType();
        channelType.setMediaRoutingDomain(UUID.randomUUID().toString());
        return channelType;
    }

    private ChannelConfig getChannelConfigInstance() {
        RoutingPolicy routingPolicy = getRoutingPolicyInstance();
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.setRoutingPolicy(routingPolicy);
        return channelConfig;
    }

    private RoutingPolicy getRoutingPolicyInstance() {
        RoutingPolicy routingPolicy = new RoutingPolicy();
        routingPolicy.setRoutingObjectId(UUID.randomUUID().toString());
        return routingPolicy;
    }
}