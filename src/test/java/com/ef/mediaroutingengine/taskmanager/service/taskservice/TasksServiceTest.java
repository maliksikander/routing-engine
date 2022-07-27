package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.taskmanager.service.TasksService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {
    TasksService tasksService;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private TasksRetrieverFactory factory;
    @Mock
    private JmsCommunicator jmsCommunicator;

    @BeforeEach
    void setUp() {
        this.tasksService = new TasksService(tasksPool, tasksRepository, factory, jmsCommunicator);
    }

    @Test
    void testRetrieveById_throwsNotFoundException_when_taskNotFoundInTasksPool() {
        UUID taskId = UUID.randomUUID();
        when(tasksPool.findById(taskId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> tasksService.retrieveById(taskId));
    }

    @Test
    void testRetrieveById_returnsTaskDto_when_taskFoundInTasksPool() {
        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        ChannelSession channelSession = mock(ChannelSession.class);
        MediaRoutingDomain mrd = mock(MediaRoutingDomain.class);
        Task task = Task.getInstanceFrom(channelSession, mrd, "queueId", taskState);

        when(tasksPool.findById(task.getId())).thenReturn(task);
        TaskDto taskDto = tasksService.retrieveById(task.getId());

        assertEquals(channelSession, taskDto.getChannelSession());
        assertEquals(mrd, taskDto.getMrd());
        assertEquals("queueId", taskDto.getQueue());
        assertEquals(taskState, taskDto.getState());
    }

    @Test
    void testRetrieve() {
        TasksRetriever tasksRetriever = mock(TasksRetriever.class);
        Optional<UUID> agentId = Optional.of(UUID.randomUUID());
        Optional<Enums.TaskStateName> taskState = Optional.of(Enums.TaskStateName.QUEUED);
        List<TaskDto> taskDtoList = new ArrayList<>();
        taskDtoList.add(new TaskDto());

        when(factory.getRetriever(agentId, taskState)).thenReturn(tasksRetriever);
        when(tasksRetriever.findTasks()).thenReturn(taskDtoList);

        List<TaskDto> result = tasksService.retrieve(agentId, taskState);
        assertEquals(taskDtoList.size(), result.size());
    }
}