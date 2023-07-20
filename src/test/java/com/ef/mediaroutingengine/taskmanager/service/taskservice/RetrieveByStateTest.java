package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.*;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetrieveByStateTest {
    RetrieveByState retrieveByState;
    @Mock
    TasksPool tasksPool;
    Enums.TaskStateName stateName = Enums.TaskStateName.QUEUED;

    @BeforeEach
    void setUp() {
        this.retrieveByState = new RetrieveByState(tasksPool, stateName);
    }

    @Test
    void testFindTasks_returnsAllTasksInTasksPoolWithSpecificState() {
        List<Task> taskList = new ArrayList<>();
        // 1 task which meets criteria
        taskList.add(getNewTask(Enums.TaskStateName.QUEUED));
        // tasks that do not meet criteria
        taskList.add(getNewTask(Enums.TaskStateName.RESERVED));
        taskList.add(getNewTask(Enums.TaskStateName.ACTIVE));

        when(tasksPool.findAll()).thenReturn(taskList);
        List<TaskDto> taskDtoList = retrieveByState.findTasks();

        assertEquals(1, taskDtoList.size());
    }

    private Task getNewTask(Enums.TaskStateName stateName) {
        TaskState taskState = new TaskState(stateName, null);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE,null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "queue1");
        return Task.getInstanceFrom(getNewChannelSession(), getNewMrd(), taskQueue, taskState,type, 1);
    }

    private ChannelSession getNewChannelSession() {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setId(UUID.randomUUID().toString());
        channelSession.setConversationId(UUID.randomUUID().toString());
        return channelSession;
    }

    private MediaRoutingDomain getNewMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        return mrd;
    }
}