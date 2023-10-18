package com.ef.mediaroutingengine.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.*;
import com.ef.mediaroutingengine.routing.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.model.Step;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
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
    private TasksPool tasksPool;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;

    private AssignResourceServiceImpl assignResourceService;

    @BeforeEach
    void setUp() {
        this.assignResourceService = new AssignResourceServiceImpl(taskManager, tasksPool, precisionQueuesPool);
    }

    @Test
    void test_assign() {
        ChannelSession channelSession = getChannelSessionInstance();
        String requestedQueue = UUID.randomUUID().toString();
        TaskType taskType = new TaskType(Enums.TaskTypeDirection.INBOUND,Enums.TaskTypeMode.QUEUE,null);
        CCUser ccUser = new CCUser();
        AssignResourceRequest request = new AssignResourceRequest(requestedQueue, channelSession, taskType, ccUser);

        MediaRoutingDomain mrd = mock(MediaRoutingDomain.class);
        PrecisionQueue queue = mock(PrecisionQueue.class);

        AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);

        doNothing().when(spy).validateChannelSession(channelSession, taskType);
        doReturn(mrd).when(queue).getMrd();
        doReturn(queue).when(spy).validateAndGetQueue(channelSession, requestedQueue, false);

        String response = spy.assign(request, false, true,1);
        assertEquals("The request is received Successfully", response);
    }

    @Nested
    @DisplayName("validateChannelSession method tests")
    class ValidateChannelSessionTest {
        @Test
        void throwsIllegalArgumentException_when_taskTypeIsInbound_and_routingModeIsNotPush() {
            ChannelSession channelSession = getChannelSessionInstance();
            TaskType taskType = new TaskType(Enums.TaskTypeDirection.INBOUND,Enums.TaskTypeMode.QUEUE,null);
            channelSession.getChannel().getChannelConfig().getRoutingPolicy().setRoutingMode(RoutingMode.PULL);
            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateChannelSession(channelSession, taskType));
        }
    }

    @Nested
    @DisplayName("validateAndGetQueue method tests")
    class ValidateAndGetQueueTest {
        @Test
        void throwsIllegalArgumentsException_when_defaultQueueIsNullAndRequestedQueueIdNull() {
            ChannelSession channelSession = getChannelSessionInstance();
            channelSession.getChannel().getChannelConfig().getRoutingPolicy().setRoutingObjectId(null);

            assertThrows(IllegalArgumentException.class,
                    () -> assignResourceService.validateAndGetQueue(channelSession, null, false));
        }

        @Test
        void throwsIllegalArgumentException_when_noQueueFoundFromPool() {
            ChannelSession channelSession = getChannelSessionInstance();

            AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);
            doReturn(null).when(spy).getPrecisionQueueFrom(any(), any(), eq(Boolean.FALSE));

            assertThrows(IllegalArgumentException.class,
                    () -> spy.validateAndGetQueue(channelSession, null, false));
        }

        @Test
        void throwsIllegalStateException_when_noStepsInQueueFoundFromPool() {
            ChannelSession channelSession = getChannelSessionInstance();
            String requestedQueue = UUID.randomUUID().toString();

            AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);
            PrecisionQueue queueFound = mock(PrecisionQueue.class);

            doReturn(queueFound).when(spy).getPrecisionQueueFrom(any(), any(), eq(Boolean.FALSE));
            when(queueFound.getSteps()).thenReturn(new ArrayList<>());

            assertThrows(IllegalStateException.class,
                    () -> spy.validateAndGetQueue(channelSession, requestedQueue, false));
        }

        @Test
        void returnsQueue_when_validationSuccessful() {
            ChannelSession channelSession = getChannelSessionInstance();
            String requestedQueue = UUID.randomUUID().toString();
            AssignResourceServiceImpl spy = Mockito.spy(assignResourceService);

            PrecisionQueue queueFound = mock(PrecisionQueue.class);
            doReturn(queueFound).when(spy).getPrecisionQueueFrom(any(), any(), eq(Boolean.FALSE));

            List<Step> stepList = new ArrayList<>();
            stepList.add(mock(Step.class));

            when(queueFound.getSteps()).thenReturn(stepList);

            PrecisionQueue result = spy.validateAndGetQueue(channelSession, requestedQueue, false);
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

            PrecisionQueue found = assignResourceService.getPrecisionQueueFrom(requestedQueueId, defaultQueueId, false);

            assertEquals(requestedQueue, found);
        }

        @Test
        void returnsDefaultQueue_when_requestedQueueNotFound() {
            String requestedQueueId = UUID.randomUUID().toString();
            String defaultQueueId = UUID.randomUUID().toString();

            PrecisionQueue defaultQueue = mock(PrecisionQueue.class);
            when(precisionQueuesPool.findById(requestedQueueId)).thenReturn(null);
            when(precisionQueuesPool.findById(defaultQueueId)).thenReturn(defaultQueue);

            PrecisionQueue found = assignResourceService.getPrecisionQueueFrom(requestedQueueId, defaultQueueId, false);

            assertEquals(defaultQueue, found);
        }

        @Test
        void returnsNull_when_requestQueueAndDefaultQueueBothNotFound() {
            String requestedQueueId = UUID.randomUUID().toString();
            String defaultQueueId = UUID.randomUUID().toString();

            when(precisionQueuesPool.findById(requestedQueueId)).thenReturn(null);
            when(precisionQueuesPool.findById(defaultQueueId)).thenReturn(null);

            PrecisionQueue found = assignResourceService.getPrecisionQueueFrom(requestedQueueId, defaultQueueId, false);

            assertNull(found);
        }
    }

    private ChannelSession getChannelSessionInstance() {
        Channel channel = getChannelInstance();
        ChannelSession channelSession = new ChannelSession();
        channelSession.setChannel(channel);
        channelSession.setConversationId(UUID.randomUUID().toString());
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