package com.ef.mediaroutingengine.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.TaskQueue;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.StepTimerService;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.queue.PriorityQueue;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrecisionQueuesServiceImplTest {

    @Mock
    private PrecisionQueueRepository repository;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private MrdPool mrdPool;
    @Mock
    private TaskManager taskManager;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private StepTimerService stepTimerService;
    @Mock
    private AgentRequestTimerService agentRequestTimerService;

    private PrecisionQueuesServiceImpl precisionQueuesService;

    @BeforeEach
    void setUp() {
        this.precisionQueuesService = new PrecisionQueuesServiceImpl(repository, precisionQueuesPool, mrdPool, tasksPool
                , taskManager, jmsCommunicator, stepTimerService, agentRequestTimerService);
    }

    @Test
    void testCreate_when_precisionQueueIsCreated() {
        PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();
        PrecisionQueueEntity precisionQueueEntity = mock(PrecisionQueueEntity.class);

        PrecisionQueuesServiceImpl spy = Mockito.spy(precisionQueuesService);

        doNothing().when(spy).throwExceptionIfQueueNameIsNotUnique(requestBody, null);
        doNothing().when(spy).validateAndSetMrd(requestBody);
        doReturn(precisionQueueEntity).when(this.repository).insert((PrecisionQueueEntity) any());
        doReturn(mock(TaskRouter.class)).when(spy).getTaskSchedulerBean();

        spy.create(requestBody);

        ArgumentCaptor<PrecisionQueue> precisionQueueRequestBodyArgumentCaptor =
                ArgumentCaptor.forClass(PrecisionQueue.class);
        verify(repository).insert((PrecisionQueueEntity) any());
        verify(precisionQueuesPool).insert(precisionQueueRequestBodyArgumentCaptor.capture());

    }

    @Test
    void testUpdate_onSuccessfulUpdate() {
        String queueId = UUID.randomUUID().toString();
        PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();

        PrecisionQueueEntity entity = new PrecisionQueueEntity();
        entity.setId(queueId);
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());

        Optional<PrecisionQueueEntity> existingEntity = Optional.of(entity);
        PrecisionQueue precisionQueue = mock(PrecisionQueue.class);

        PrecisionQueuesServiceImpl spy = Mockito.spy(precisionQueuesService);

        doNothing().when(spy).throwExceptionIfQueueNameIsNotUnique(requestBody, queueId);
        doNothing().when(spy).validateAndSetMrd(requestBody);
        doReturn(existingEntity).when(this.repository).findById(queueId);
        doReturn(precisionQueue).when(this.precisionQueuesPool).findById(queueId);

        PrecisionQueueEntity result = spy.update(requestBody, queueId);

        verify(precisionQueue, times(1)).updateQueue(requestBody);
        verify(this.repository, times(1)).save(existingEntity.get());
        assertEquals(queueId, result.getId());
    }

    @Test
    void testDelete_when_precisionQueueIsDeleted() {
        String queueId = UUID.randomUUID().toString();
        doReturn(true).when(this.repository).existsById(queueId);
        doReturn(new ArrayList<>()).when(this.tasksPool).findByQueueId(queueId);
        doReturn(mock(PrecisionQueue.class)).when(this.precisionQueuesPool).findById(queueId);

        ResponseEntity<Object> response = precisionQueuesService.delete(queueId);

        verify(precisionQueuesPool).deleteById(queueId);
        verify(repository).deleteById(queueId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Nested
    @DisplayName(" Flush Queue Tests")
    class FlushQueueTest {
        @Test
        void when_flushQueue_then_removesEnqueuedTasks() {
            String topicId = UUID.randomUUID().toString();
            PrecisionQueue precisionQueue = mock(PrecisionQueue.class);
            PriorityQueue serviceQueue = mock(PriorityQueue.class);

            TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
            Task task = getTaskInstance(topicId, taskState);

            when(precisionQueue.getServiceQueue()).thenReturn(serviceQueue);
            when(precisionQueue.getTasks()).thenReturn(Arrays.asList(task));
            when(precisionQueue.getServiceQueue()).thenReturn(serviceQueue);

            precisionQueuesService.flushQueue(precisionQueue, 0);

            verify(serviceQueue, times(1)).remove(task);

            assertEquals(Enums.TaskStateName.CLOSED, task.getTaskState().getName());
            assertEquals(Enums.TaskStateReasonCode.FORCE_CLOSED, task.getTaskState().getReasonCode());

            verify(taskManager).removeFromPoolAndRepository(task);
            verify(jmsCommunicator).publishTaskStateChangeForReporting(task);
        }
    }

    @Nested
    @DisplayName("test retrieve method")
    class TestRetrieve {
        @Test
        void when_queueNotExistInRepository() {
            PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();
            PrecisionQueueEntity entity = new PrecisionQueueEntity();
            entity.setId(requestBody.getId());
            entity.setName(requestBody.getName());
            entity.setMrd(requestBody.getMrd());
            entity.setServiceLevelType(requestBody.getServiceLevelType());
            entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
            String queueId = entity.getId();

            assertThrows(NotFoundException.class, () -> precisionQueuesService.retrieve(queueId));
        }

        @Test
        void when_precisionQueueExistInRepository() {
            String queueId = UUID.randomUUID().toString();
            PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();
            PrecisionQueueEntity entity = new PrecisionQueueEntity();
            entity.setId(queueId);
            entity.setName(requestBody.getName());
            entity.setMrd(requestBody.getMrd());
            entity.setServiceLevelType(requestBody.getServiceLevelType());
            entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());

            doReturn(false).when(repository).existsById(queueId);
            doReturn(true).when(repository).existsById(queueId);
            doReturn(Optional.of(entity)).when(repository).findById(queueId);

            PrecisionQueuesServiceImpl spy = Mockito.spy(precisionQueuesService);

            ResponseEntity<Object> result = spy.retrieve(queueId);
            assertEquals(HttpStatus.OK, result.getStatusCode());

        }
    }

    private PrecisionQueueRequestBody getPrecisionQueueRequest() {
        PrecisionQueueRequestBody precisionQueueRequestBody = new PrecisionQueueRequestBody();
        precisionQueueRequestBody.setId(UUID.randomUUID().toString());
        precisionQueueRequestBody.setName("test");
        precisionQueueRequestBody.setMrd(getMRD());
        precisionQueueRequestBody.setServiceLevelThreshold(3);
        return precisionQueueRequestBody;
    }

    private MediaRoutingDomain getMRD() {
        MediaRoutingDomain mediaRoutingDomain = new MediaRoutingDomain();
        mediaRoutingDomain.setId(UUID.randomUUID().toString());
        mediaRoutingDomain.setName("chat");
        return mediaRoutingDomain;
    }

    private Task getTaskInstance(String topicId, TaskState taskState) {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setConversationId(topicId);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "queue1");
        return Task.getInstanceFrom(channelSession, null, taskQueue, taskState, type, 1);
    }


}

