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
class RetrieveByAgentTest {
    RetrieveByAgent retrieveByAgent;
    @Mock
    TasksPool tasksPool;
    String agentId = UUID.randomUUID().toString();

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
        taskList.add(getNewTask(UUID.randomUUID().toString()));

        when(tasksPool.findAll()).thenReturn(taskList);

        List<TaskDto> result = retrieveByAgent.findTasks();
        assertEquals(2, result.size());
    }

    private Task getNewTask(String assignedTo) {
        TaskState taskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.AGENT,null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "queue1");
        Task task = Task.getInstanceFrom(getNewChannelSession(), getNewMrd(), taskQueue, taskState,type);
        task.setAssignedTo(assignedTo);
        return task;
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