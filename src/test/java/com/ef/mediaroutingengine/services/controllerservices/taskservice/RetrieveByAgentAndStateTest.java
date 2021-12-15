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
class RetrieveByAgentAndStateTest {
    RetrieveByAgentAndState retrieveByAgentAndState;
    @Mock
    TasksPool tasksPool;
    UUID agentId = UUID.randomUUID();
    Enums.TaskStateName stateName = Enums.TaskStateName.QUEUED;

    @BeforeEach
    void setUp() {
        this.retrieveByAgentAndState = new RetrieveByAgentAndState(tasksPool, agentId, stateName);
    }

    @Test
    void testFindTasks_returnsAllTasksInTasksPoolWithSpecificStateAndAssignedToSpecificAgent() {
        List<Task> taskList = new ArrayList<>();
        // 2 tasks that meet our criteria
        taskList.add(getNewTask(agentId, Enums.TaskStateName.QUEUED));
        taskList.add(getNewTask(agentId, Enums.TaskStateName.QUEUED));
        // Tasks that do not meet our criteria
        taskList.add(getNewTask(agentId, Enums.TaskStateName.RESERVED));
        taskList.add(getNewTask(UUID.randomUUID(), Enums.TaskStateName.QUEUED));
        taskList.add(getNewTask(null, Enums.TaskStateName.QUEUED));

        when(tasksPool.findAll()).thenReturn(taskList);
        List<TaskDto> taskDtoList = retrieveByAgentAndState.findTasks();

        assertEquals(2, taskDtoList.size());
    }

    private Task getNewTask(UUID assignedTo, Enums.TaskStateName stateName) {
        TaskState taskState = new TaskState(stateName, null);
        Task task = Task.getInstanceFrom(getNewChannelSession(), getNewMrd(), "queue", taskState);
        task.setAssignedTo(assignedTo);
        return task;
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