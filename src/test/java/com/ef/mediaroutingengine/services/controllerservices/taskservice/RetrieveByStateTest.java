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
        return new Task(getNewChannelSession(), getNewMrd(), "queue", taskState);
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