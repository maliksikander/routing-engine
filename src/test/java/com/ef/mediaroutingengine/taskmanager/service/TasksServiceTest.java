package com.ef.mediaroutingengine.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskQueue;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.cim.objectmodel.dto.QueueHistoricalStatsDto;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.dto.TaskEwtAndPositionResponse;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.*;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetriever;
import com.ef.mediaroutingengine.taskmanager.service.taskservice.TasksRetrieverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {
    TasksService tasksService;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private PrecisionQueuesPool queuePool;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private TasksRetrieverFactory factory;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private RestRequest restRequest;

    @BeforeEach
    void setUp() {
        this.tasksService = new TasksService(tasksPool, queuePool, tasksRepository, factory, jmsCommunicator, restRequest);
    }

    @Test
    void testRetrieveById_throwsNotFoundException_when_taskNotFoundInTasksPool() {
        String taskId = UUID.randomUUID().toString();
        when(tasksPool.findById(taskId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> tasksService.retrieveById(taskId));
    }

    @Test
    void testRetrieveById_returnsTaskDto_when_taskFoundInTasksPool() {
        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        ChannelSession channelSession = mock(ChannelSession.class);
        MediaRoutingDomain mrd = mock(MediaRoutingDomain.class);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "queue1");
        Task task = Task.getInstanceFrom(channelSession, mrd, taskQueue, taskState, type, 1);

        when(tasksPool.findById(task.getId())).thenReturn(task);
        TaskDto taskDto = tasksService.retrieveById(task.getId());

        assertEquals(channelSession, taskDto.getChannelSession());
        assertEquals(mrd, taskDto.getMrd());
        assertEquals("queue1", taskDto.getQueue().getName());
        assertEquals(taskState, taskDto.getState());
    }

    @Test
    void testRetrieve() {
        TasksRetriever tasksRetriever = mock(TasksRetriever.class);
        Optional<String> agentId = Optional.of(UUID.randomUUID().toString());
        Optional<Enums.TaskStateName> taskState = Optional.of(Enums.TaskStateName.QUEUED);
        List<TaskDto> taskDtoList = new ArrayList<>();
        taskDtoList.add(new TaskDto());

        when(factory.getRetriever(agentId, taskState)).thenReturn(tasksRetriever);
        when(tasksRetriever.findTasks()).thenReturn(taskDtoList);

        List<TaskDto> result = tasksService.retrieve(agentId, taskState);
        assertEquals(taskDtoList.size(), result.size());
    }

    @Test
    void TestTaskPosition() {
        List<Task> taskList = new ArrayList<>();
        PrecisionQueue precisionQueue = mock(PrecisionQueue.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "Chat");

        Task task1 = Task.getInstanceFrom(getNewChannelSession(), getMrd(), taskQueue, taskState, type, 1);
        Task task2 = Task.getInstanceFrom(getNewChannelSession(), getMrd(), taskQueue, taskState, type, 1);
        Task task3 = Task.getInstanceFrom(getNewChannelSession(), getMrd(), taskQueue, taskState, type, 1);
        Task task4 = Task.getInstanceFrom(getNewChannelSession(), getMrd(), taskQueue, taskState, type, 1);

        taskList.add(task4);
        taskList.add(task3);
        taskList.add(task2);
        taskList.add(task1);

        when(queuePool.findById(taskQueue.getId())).thenReturn(precisionQueue);
        when(precisionQueue.getTasks()).thenReturn(taskList);
        int actualPosition = tasksService.getTaskPosition(task3);
        assertEquals(2, actualPosition);
        assertThrows(IllegalArgumentException.class, () -> {
            tasksService.getTaskPosition(null);
        });
    }

    @Nested
    @DisplayName("Ewt and Position Tests")
    class GetEwtAndPosition_Tests {
        @Test
        void whenNoQueuedTaskExist() {
            //given
            String conversationId = UUID.randomUUID().toString();
            when(tasksPool.findQueuedTasksFor(conversationId)).thenReturn(Collections.EMPTY_LIST);

            //when
            ResponseEntity<Object> response = tasksService.getTaskEwtAndPosition(conversationId);

            //then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(List.class);
            verifyNoMoreInteractions(queuePool);
        }

    }

    @Nested
    @DisplayName("Tests for calculating EWT and Position")
    class CalculateEwtAndPosition {
        @Test
        void test_whenAssociatedAgentsAreZero() {
            //given
            Task task = getTask(Enums.TaskStateName.QUEUED, Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE);
            PrecisionQueue precisionQueue = mock(PrecisionQueue.class);
            QueueHistoricalStatsDto queueHistoricalStats = new QueueHistoricalStatsDto();
            com.ef.cim.objectmodel.dto.QueueDto queueDto = new com.ef.cim.objectmodel.dto.QueueDto(UUID.randomUUID().toString(), "chat");
            queueHistoricalStats.setQueue(queueDto);
            queueHistoricalStats.setAverageHandleTime(0);
            queueHistoricalStats.setAverageWaitTime(0);
            List<Task> tasksInQueue = new ArrayList<>();
            tasksInQueue.add(task);

            when(queuePool.findById(task.getQueue().getId())).thenReturn(precisionQueue);
            when(precisionQueue.getTasks()).thenReturn(tasksInQueue);
            when(precisionQueue.getAssociatedAgents()).thenReturn(Collections.EMPTY_LIST);

            //when
            TaskEwtAndPositionResponse response = tasksService.calculateTaskEwtAndPosition(task, queueHistoricalStats);

            //then
            assertThat(response.getEwt()).isEqualTo(2147483647);
        }
    }

    private ChannelSession getNewChannelSession() {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setId(UUID.randomUUID().toString());
        channelSession.setConversationId(UUID.randomUUID().toString());
        return channelSession;
    }

    private MediaRoutingDomain getMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        return mrd;
    }

    private Task getTask(Enums.TaskStateName state, Enums.TaskTypeDirection direction, Enums.TaskTypeMode mode) {
        TaskState taskState = new TaskState(state, null);
        TaskType type = new TaskType(direction, mode, null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "Chat");
        Task task = Task.getInstanceFrom(getNewChannelSession(), getMrd(), taskQueue, taskState, type, 4);
        return task;
    }
}