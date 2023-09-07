package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStateCloseTest {
    private TaskStateClose taskStateClose;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private TaskManager taskManager;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private AgentRequestTimerService agentRequestTimerService;

    @BeforeEach
    void setUp() {
        this.taskStateClose = new TaskStateClose(precisionQueuesPool, tasksPool, taskManager, jmsCommunicator,
                agentRequestTimerService);
    }

    @Test
    void testUpdateState_when_reasonCodeIsNullOrNotRonaAndInProcessTaskExists() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, null);
        String conversationId = UUID.randomUUID().toString();

        when(task.getTopicId()).thenReturn(conversationId);
        when(tasksPool.findInProcessTaskFor(conversationId)).thenReturn(mock(Task.class));

        this.taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
        verify(taskManager, times(1)).endTaskFromAssignedAgent(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }

    @Test
    void testUpdateState_when_reasonCodeIsNullOrRonaAndInProcessTaskDoesNotExist() {
        Task task = mock(Task.class);
        String conversationId = UUID.randomUUID().toString();
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, null);

        when(task.getTopicId()).thenReturn(conversationId);
        when(tasksPool.findInProcessTaskFor(conversationId)).thenReturn(null);

        this.taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);

        verify(taskManager, times(1)).endTaskFromAssignedAgent(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }

    @Test
    void testUpdateState_when_reasonCodeIsRona() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.RONA);

        taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
        verify(taskManager, times(1)).endTaskFromAgentOnRona(task);
        verify(taskManager, times(1)).rerouteReservedTask(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }
}