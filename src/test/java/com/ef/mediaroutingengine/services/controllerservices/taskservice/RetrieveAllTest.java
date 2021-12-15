package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.pools.TasksPool;
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
        taskList.add(Task.getInstanceFrom(getNewChannelSession(), getNewMrd(), "queue", taskState));

        when(tasksPool.findAll()).thenReturn(taskList);

        List<TaskDto> result = retrieveAll.findTasks();
        assertEquals(taskList.size(), result.size());
    }

    private ChannelSession getNewChannelSession() {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setId(UUID.randomUUID());
        channelSession.setTopicId(UUID.randomUUID());
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