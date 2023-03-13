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
class RetrieveAllTest {
    RetrieveAll retrieveAll;
    @Mock
    TasksPool tasksPool;

    @BeforeEach
    void setUp() {
        this.retrieveAll = new RetrieveAll(tasksPool);
    }

    @Test
    void testFindTasks_returnsAllTasksInTasksPool() {
        List<Task> taskList = new ArrayList<>();
        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE,null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "queue1");
        taskList.add(Task.getInstanceFrom(getNewChannelSession(), getNewMrd(), taskQueue, taskState,type));

        when(tasksPool.findAll()).thenReturn(taskList);

        List<TaskDto> result = retrieveAll.findTasks();
        assertEquals(taskList.size(), result.size());
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