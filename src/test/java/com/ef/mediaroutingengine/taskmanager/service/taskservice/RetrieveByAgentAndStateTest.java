package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskAgent;
import com.ef.cim.objectmodel.TaskQueue;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
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
class RetrieveByAgentAndStateTest {
    RetrieveByAgentAndState retrieveByAgentAndState;
    @Mock
    TasksPool tasksPool;
    String agentId = UUID.randomUUID().toString();
    Enums.TaskStateName stateName = Enums.TaskStateName.QUEUED;

    @BeforeEach
    void setUp() {
        this.retrieveByAgentAndState = new RetrieveByAgentAndState(tasksPool, agentId, stateName);
    }

    @Test
    void testFindTasks_returnsAllTasksInTasksPoolWithSpecificStateAndAssignedToSpecificAgent() {
        List<Task> taskList = new ArrayList<>();
        // 2 tasks that meet our criteria
        taskList.add(getNewTask(createTaskAgent(agentId), Enums.TaskStateName.QUEUED));
        taskList.add(getNewTask(createTaskAgent(agentId), Enums.TaskStateName.QUEUED));
        // Tasks that do not meet our criteria
        taskList.add(getNewTask(createTaskAgent(agentId), Enums.TaskStateName.RESERVED));
        taskList.add(getNewTask(createTaskAgent(UUID.randomUUID().toString()), Enums.TaskStateName.QUEUED));
        taskList.add(getNewTask(null, Enums.TaskStateName.QUEUED));

        when(tasksPool.findAll()).thenReturn(taskList);
        List<TaskDto> taskDtoList = retrieveByAgentAndState.findTasks();

        assertEquals(2, taskDtoList.size());
    }

    private Task getNewTask(TaskAgent assignedTo, Enums.TaskStateName stateName) {
        TaskState taskState = new TaskState(stateName, null);
        TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
        TaskQueue taskQueue = new TaskQueue(UUID.randomUUID().toString(), "queue1");
        Task task = Task.getInstanceFrom(getNewChannelSession(), getNewMrd(), taskQueue, taskState, type);
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

    private TaskAgent createTaskAgent(String id) {
        return new TaskAgent(id, null);
    }
}