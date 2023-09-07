package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskAgent;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStateActiveTest {
    private TaskStateActive taskStateActive;
    @Mock
    private TaskManager taskManager;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private AgentRequestTimerService agentRequestTimerService;

    @BeforeEach
    void setUp() {
        this.taskStateActive = new TaskStateActive(taskManager, agentsPool, tasksRepository, jmsCommunicator,
                agentRequestTimerService);
    }

    @Test
    void test_updateState_when_agentIsNull() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        TaskAgent taskAgent = new TaskAgent(UUID.randomUUID().toString(), "agent1");

        when(task.getAssignedTo()).thenReturn(taskAgent);
        when(agentsPool.findBy(taskAgent)).thenReturn(null);

        taskStateActive.updateState(task, taskState);

        verifyNoMoreInteractions(task);
        verifyNoInteractions(taskManager);
    }

    @Test
    void test_updateState_when_agentIsNotNull() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        Agent agent = mock(Agent.class);
        String topicId = UUID.randomUUID().toString();
        MediaRoutingDomain mrd = getNewMrd();
        TaskAgent taskAgent = new TaskAgent(UUID.randomUUID().toString(), "agent1");

        when(task.getTaskState()).thenReturn(new TaskState(Enums.TaskStateName.RESERVED, null));
        when(task.getAssignedTo()).thenReturn(taskAgent);
        when(agentsPool.findBy(taskAgent)).thenReturn(agent);

        when(task.getType()).thenReturn(new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null));
        when(task.getId()).thenReturn(UUID.randomUUID().toString());
        when(task.getTopicId()).thenReturn(topicId).thenReturn(topicId);
        when(task.getMrd()).thenReturn(mrd);


        taskStateActive.updateState(task, taskState);

        verify(task, times(1)).setTaskState(taskState);
        verify(task, times(1)).setStartTime(anyLong());
        verify(tasksRepository, times(1)).save(any(), any());
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
        verify(agent, times(1)).removeReservedTask();
        verify(agent, times(1)).addActiveTask(task);
        verify(taskManager, times(1)).updateAgentMrdState(agent, mrd.getId());

        verifyNoMoreInteractions(taskManager);
        verifyNoMoreInteractions(agent);

    }

    private MediaRoutingDomain getNewMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        return mrd;
    }
}