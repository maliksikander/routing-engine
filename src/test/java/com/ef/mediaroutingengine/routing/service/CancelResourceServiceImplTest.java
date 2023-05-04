package com.ef.mediaroutingengine.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskAgent;
import com.ef.cim.objectmodel.TaskQueue;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.dto.CancelResourceRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.queue.PriorityQueue;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
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
class CancelResourceServiceImplTest {
    @Mock
    private TasksPool tasksPool;
    @Mock
    private TaskManager taskManager;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private RestRequest restRequest;
    @Mock
    private JmsCommunicator jmsCommunicator;
    private CancelResourceServiceImpl cancelResourceService;

    @BeforeEach
    void setUp() {
        this.cancelResourceService = new CancelResourceServiceImpl(tasksPool, taskManager, precisionQueuesPool,
                agentsPool, restRequest, jmsCommunicator);
    }

    @Nested
    @DisplayName("cancelResource method tests")
    class CancelResourceTest {
        @Test
        void returns_withoutDoingAnything_when_taskIsNull() {
            CancelResourceRequest request = getCancelResourceRequestInstance();
            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            when(tasksPool.findInProcessTaskFor(request.getTopicId())).thenReturn(null);

            spy.cancelResource(request);
            verifyNoMoreInteractions(taskManager);
        }

        @Test
        void when_cancelQueuedTaskSuccessfully() {
            CancelResourceRequest request = getCancelResourceRequestInstance();

            TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
            Task task = getTaskInstance(request.getTopicId(), taskState);

            PrecisionQueue precisionQueue = mock(PrecisionQueue.class);
            PriorityQueue serviceQueue = mock(PriorityQueue.class);

            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            when(tasksPool.findInProcessTaskFor(request.getTopicId())).thenReturn(task);
            when(precisionQueuesPool.findById(task.getQueue().getId())).thenReturn(precisionQueue);
            when(precisionQueue.getServiceQueue()).thenReturn(serviceQueue);
            doNothing().when(spy).endQueuedTask(task, precisionQueue, request.getReasonCode());

            spy.cancelResource(request);

            verify(taskManager, times(1)).cancelAgentRequestTtlTimerTask(request.getTopicId());
            verify(taskManager, times(1)).removeAgentRequestTtlTimerTask(request.getTopicId());
        }

        @Test
        void when_cancelReservedTaskSuccessfully() {
            CancelResourceRequest request = getCancelResourceRequestInstance();
            TaskState taskState = new TaskState(Enums.TaskStateName.RESERVED, null);
            Task task = getTaskInstance(request.getTopicId(), taskState);
            PrecisionQueue precisionQueue = mock(PrecisionQueue.class);
            PriorityQueue serviceQueue = mock(PriorityQueue.class);
            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            when(tasksPool.findInProcessTaskFor(request.getTopicId())).thenReturn(task);
            when(precisionQueuesPool.findById(task.getQueue().getId())).thenReturn(precisionQueue);
            when(precisionQueue.getServiceQueue()).thenReturn(serviceQueue);
            doNothing().when(spy).endReservedTask(task, request.getReasonCode());

            spy.cancelResource(request);

            verify(taskManager, times(1)).cancelAgentRequestTtlTimerTask(request.getTopicId());
            verify(taskManager, times(1)).removeAgentRequestTtlTimerTask(request.getTopicId());
        }
    }

    @Test
    void test_endQueuedTask() {
        Task task = mock(Task.class);
        Enums.TaskStateReasonCode closeReasonCode = Enums.TaskStateReasonCode.CANCELLED;
        PrecisionQueue precisionQueue = mock(PrecisionQueue.class);

        CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);
        doNothing().when(spy).removeAndPublish(task, closeReasonCode);
        when(precisionQueue.getTaskScheduler()).thenReturn(mock(TaskRouter.class));

        spy.endQueuedTask(task, precisionQueue, closeReasonCode);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(task, times(1)).removePropertyChangeListener(captor.capture(), any());
        assertEquals(Enums.EventName.STEP_TIMEOUT.name(), captor.getValue());
    }

    @Nested
    @DisplayName("endReservedTask method tests")
    class EndReservedTaskTest {
        @Test
        void removesTask_when_revokeTaskApiReturnsSuccessResponse() {
            Task task = mock(Task.class);
            Enums.TaskStateReasonCode closeReasonCode = Enums.TaskStateReasonCode.CANCELLED;
            Agent agent = mock(Agent.class);
            TaskAgent taskAgent = new TaskAgent(UUID.randomUUID().toString(), "agent1");

            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            doNothing().when(spy).removeAndPublish(task, closeReasonCode);
            when(task.getAssignedTo()).thenReturn(taskAgent);
            doReturn(agent).when(agentsPool).findBy(taskAgent);

            spy.endReservedTask(task, closeReasonCode);
            verify(agent, times(1)).removeReservedTask();
        }
    }

    @Test
    void test_removeAndPublish() {
        Task task = mock(Task.class);
        String taskId = UUID.randomUUID().toString();
        Enums.TaskStateReasonCode closeReasonCode = Enums.TaskStateReasonCode.CANCELLED;

        when(task.getId()).thenReturn(taskId);

        cancelResourceService.removeAndPublish(task, closeReasonCode);

        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
    }

    private CancelResourceRequest getCancelResourceRequestInstance() {
        CancelResourceRequest request = new CancelResourceRequest();
        request.setReasonCode(Enums.TaskStateReasonCode.CANCELLED);
        request.setTopicId(UUID.randomUUID().toString());
        return request;
    }

    private Task getTaskInstance(String topicId, TaskState taskState) {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setConversationId(topicId);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE,null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "Queue1");
        return Task.getInstanceFrom(channelSession, null, taskQueue, taskState,type);
    }
}