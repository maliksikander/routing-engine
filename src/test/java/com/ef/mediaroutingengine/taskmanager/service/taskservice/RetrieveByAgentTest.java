package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskState;
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
class RetrieveByAgentTest {
    RetrieveByAgent retrieveByAgent;
    @Mock
    TasksPool tasksPool;
    UUID agentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        this.retrieveByAgent = new RetrieveByAgent(tasksPool, agentId);
    }

    @Test
    void testFindTasks_returnsAllTasksInTasksPoolAssignedToSpecificAgent() {
        List<Task> taskList = new ArrayList<>();
        /*
        2 tasks assigned to agent we are looking the tasks for.
        1 task assigned to no one
        1 to another agent
         */
        taskList.add(getNewTask(agentId));
        taskList.add(getNewTask(agentId));
        taskList.add(getNewTask(null));
        taskList.add(getNewTask(UUID.randomUUID()));

        when(tasksPool.findAll()).thenReturn(taskList);

        List<TaskDto> result = retrieveByAgent.findTasks();
        assertEquals(2, result.size());
    }

    private Task getNewTask(UUID assignedTo) {
        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        Task task = Task.getInstanceFrom(getNewChannelSession(), getNewMrd(), "queue", taskState);
        task.setAssignedTo(assignedTo);
        return task;
    }

    private ChannelSession getNewChannelSession() {
        ChannelSession channelSession = new ChannelSession();
        channelSession.setId(UUID.randomUUID());
        channelSession.setConversationId(UUID.randomUUID());
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