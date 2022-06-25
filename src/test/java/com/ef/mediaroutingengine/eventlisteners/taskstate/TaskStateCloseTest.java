package com.ef.mediaroutingengine.eventlisteners.taskstate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
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

    @BeforeEach
    void setUp() {
        this.taskStateClose = new TaskStateClose(precisionQueuesPool, taskManager);
    }

    @Test
    void testUpdateState_when_reasonCodeIsNullOrRonaAndRoutingModeIsNotPush() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, null);

        when(task.getRoutingMode()).thenReturn(RoutingMode.PULL);
        this.taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
        verify(taskManager, times(1)).endTaskFromAssignedAgent(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }

    @Test
    void testUpdateState_when_reasonCodeIsNullOrRonaAndRoutingModeIsPush() {
        Task task = mock(Task.class);
        UUID topicId = UUID.randomUUID();
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, null);

        when(task.getRoutingMode()).thenReturn(RoutingMode.PUSH);
        when(task.getTopicId()).thenReturn(topicId);
        this.taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).removeFromPoolAndRepository(task);
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

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }
}