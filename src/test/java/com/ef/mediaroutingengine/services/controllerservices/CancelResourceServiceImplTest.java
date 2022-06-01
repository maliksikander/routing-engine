package com.ef.mediaroutingengine.services.controllerservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.CancelResourceRequest;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.TaskRouter;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.queue.PriorityQueue;
import com.ef.mediaroutingengine.services.utilities.RestRequest;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
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
    private JmsCommunicator jmsCommunicator;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private RestRequest restRequest;

    private CancelResourceServiceImpl cancelResourceService;

    @BeforeEach
    void setUp() {
        this.cancelResourceService = new CancelResourceServiceImpl(tasksPool, taskManager, precisionQueuesPool,
                jmsCommunicator, tasksRepository, agentsPool, restRequest);
    }

    @Nested
    @DisplayName("cancelResource method tests")
    class CancelResourceTest {
        @Test
        void returns_withoutDoingAnything_when_taskIsNotProcessable() {
            CancelResourceRequest request = getCancelResourceRequestInstance();
            Task task = mock(Task.class);
            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            when(tasksPool.findFirstByConversationId(request.getTopicId())).thenReturn(task);
            doReturn(false).when(spy).isProcessable(task);

            spy.cancelResource(request);
            verifyNoMoreInteractions(task);
        }

        @Test
        void when_cancelQueuedTaskSuccessfully() {
            CancelResourceRequest request = getCancelResourceRequestInstance();
            TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
            Task task = getTaskInstance(request.getTopicId(), taskState);
            PrecisionQueue precisionQueue = mock(PrecisionQueue.class);
            PriorityQueue serviceQueue = mock(PriorityQueue.class);
            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            when(tasksPool.findFirstByConversationId(request.getTopicId())).thenReturn(task);
            doReturn(true).when(spy).isProcessable(task);
            when(precisionQueuesPool.findById(task.getQueue())).thenReturn(precisionQueue);
            when(precisionQueue.getServiceQueue()).thenReturn(serviceQueue);
            doNothing().when(spy).endQueuedTask(task, precisionQueue, request.getReasonCode());

            spy.cancelResource(request);

            verify(taskManager, times(1)).cancelAgentRequestTtlTimerTask(request.getTopicId());
            verify(taskManager, times(1)).removeAgentRequestTtlTimerTask(request.getTopicId());
            verify(precisionQueue, times(1)).removeTask(task);
        }

        @Test
        void when_cancelReservedTaskSuccessfully() {
            CancelResourceRequest request = getCancelResourceRequestInstance();
            TaskState taskState = new TaskState(Enums.TaskStateName.RESERVED, null);
            Task task = getTaskInstance(request.getTopicId(), taskState);
            PrecisionQueue precisionQueue = mock(PrecisionQueue.class);
            PriorityQueue serviceQueue = mock(PriorityQueue.class);
            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            when(tasksPool.findFirstByConversationId(request.getTopicId())).thenReturn(task);
            doReturn(true).when(spy).isProcessable(task);
            when(precisionQueuesPool.findById(task.getQueue())).thenReturn(precisionQueue);
            when(precisionQueue.getServiceQueue()).thenReturn(serviceQueue);
            doNothing().when(spy).endReservedTask(task, request.getReasonCode());

            spy.cancelResource(request);

            verify(taskManager, times(1)).cancelAgentRequestTtlTimerTask(request.getTopicId());
            verify(taskManager, times(1)).removeAgentRequestTtlTimerTask(request.getTopicId());
            verify(precisionQueue, times(1)).removeTask(task);
        }
    }

    @Nested
    @DisplayName("isProcessable method tests")
    class IsProcessableTest {
        @Test
        void returnsFalse_when_taskIsNull() {
            assertFalse(cancelResourceService.isProcessable(null));
        }

        @Test
        void returnsFalse_when_taskStateIsNotQueuedOrReserved() {
            Task task = mock(Task.class);
            TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);

            when(task.getTaskState()).thenReturn(taskState);
            assertFalse(cancelResourceService.isProcessable(task));
        }

        @Test
        void returnsFalse_when_taskIsAlreadyMarkedForDeletion() {
            Task task = mock(Task.class);
            TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);

            when(task.getTaskState()).thenReturn(taskState);
            when(task.isMarkedForDeletion()).thenReturn(true);
            assertFalse(cancelResourceService.isProcessable(task));
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
            UUID agentId = UUID.randomUUID();

            CancelResourceServiceImpl spy = Mockito.spy(cancelResourceService);

            when(restRequest.postRevokeTask(task)).thenReturn(true);
            doNothing().when(spy).removeAndPublish(task, closeReasonCode);
            when(task.getAssignedTo()).thenReturn(agentId);
            doReturn(agent).when(agentsPool).findById(agentId);

            spy.endReservedTask(task, closeReasonCode);

            verify(agent, times(1)).removeReservedTask();
        }

        @Test
        void marksTaskForDeletion_when_revokeTaskApiDoesNotReturnsSuccessResponse() {
            Task task = mock(Task.class);
            Enums.TaskStateReasonCode closeReasonCode = Enums.TaskStateReasonCode.CANCELLED;

            when(restRequest.postRevokeTask(task)).thenReturn(false);

            cancelResourceService.endReservedTask(task, closeReasonCode);
            verify(task, times(1)).markForDeletion(closeReasonCode);
        }
    }

    @Test
    void test_removeAndPublish() {
        Task task = mock(Task.class);
        UUID taskId = UUID.randomUUID();
        Enums.TaskStateReasonCode closeReasonCode = Enums.TaskStateReasonCode.CANCELLED;

        when(task.getId()).thenReturn(taskId);

        cancelResourceService.removeAndPublish(task, closeReasonCode);

        verify(tasksPool, times(1)).remove(task);
        verify(tasksRepository, times(1)).deleteById(taskId.toString());

        ArgumentCaptor<TaskState> captor = ArgumentCaptor.forClass(TaskState.class);
        verify(task, times(1)).setTaskState(captor.capture());
        assertEquals(Enums.TaskStateName.CLOSED, captor.getValue().getName());
        assertEquals(closeReasonCode, captor.getValue().getReasonCode());

        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
    }

    private CancelResourceRequest getCancelResourceRequestInstance() {
        CancelResourceRequest request = new CancelResourceRequest();
        request.setReasonCode(Enums.TaskStateReasonCode.CANCELLED);
        request.setTopicId(UUID.randomUUID());
        return request;
    }

    private Task getTaskInstance(UUID topicId, TaskState taskState) {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setConversationId(topicId);
        return Task.getInstanceFrom(channelSession, null, UUID.randomUUID().toString(), taskState);
    }
}