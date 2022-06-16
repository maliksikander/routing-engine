package com.ef.mediaroutingengine.eventlisteners.taskstate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.TasksRepository;
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
    private TasksRepository tasksRepository;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        this.taskStateClose = new TaskStateClose(tasksRepository, precisionQueuesPool, taskManager);
    }

    @Test
    void testUpdateState_when_requestedStateReasonCodeIsNullOrIsNotRona() {
        Task task = mock(Task.class);
        UUID taskId = UUID.randomUUID();
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, null);

        when(task.getId()).thenReturn(taskId);
        taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).endTaskFromAssignedAgent(task);
        verify(tasksRepository, times(1)).deleteById(taskId.toString());
        verify(taskManager, times(1)).removeTask(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
        verifyNoMoreInteractions(tasksRepository);
    }

    @Test
    void testUpdateState_when_requestedStateReasonCodeIsRona() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.RONA);

        taskStateClose.updateState(task, taskState);

        verify(precisionQueuesPool, times(1)).endTask(task);
        verify(taskManager, times(1)).endTaskFromAgentOnRona(task);
        verify(taskManager, times(1)).rerouteReservedTask(task);

        verifyNoMoreInteractions(precisionQueuesPool);
        verifyNoMoreInteractions(taskManager);
    }
}