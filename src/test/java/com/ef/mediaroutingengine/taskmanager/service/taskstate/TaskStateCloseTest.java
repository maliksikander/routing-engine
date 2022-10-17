package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
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
    private TaskManager taskManager;
    @Mock
    private JmsCommunicator jmsCommunicator;

    @BeforeEach
    void setUp() {
        this.taskStateClose = new TaskStateClose(precisionQueuesPool, taskManager, jmsCommunicator);
    }

    @Test
    void testUpdateState_when_reasonCodeIsNullOrRonaAndRoutingModeIsNotPush() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, null);

        when(task.getRoutingMode()).thenReturn(RoutingMode.PULL);
        this.taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
        verify(taskManager, times(1)).endTaskFromAssignedAgent(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }

    @Test
    void testUpdateState_when_reasonCodeIsNullOrRonaAndRoutingModeIsPush() {
        Task task = mock(Task.class);
        String topicId = UUID.randomUUID().toString();
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, null);

        when(task.getRoutingMode()).thenReturn(RoutingMode.PUSH);
        when(task.getTopicId()).thenReturn(topicId);
        this.taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
        verify(taskManager, times(1)).cancelAgentRequestTtlTimerTask(topicId);
        verify(taskManager, times(1)).removeAgentRequestTtlTimerTask(topicId);
        verify(taskManager, times(1)).endTaskFromAssignedAgent(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }

    @Test
    void testUpdateState_when_reasonCodeIsRonaAndRoutingModeIsPush() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.RONA);

        when(task.getRoutingMode()).thenReturn(RoutingMode.PUSH);
        taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
        verify(taskManager, times(1)).endTaskFromAgentOnRona(task);
        verify(taskManager, times(1)).rerouteReservedTask(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }

    @Test
    void testUpdateState_when_reasonCodeIsRonaAndRoutingModeIsNotPush() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.RONA);

        when(task.getRoutingMode()).thenReturn(RoutingMode.EXTERNAL);
        taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }
}