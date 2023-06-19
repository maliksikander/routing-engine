package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class TaskStateOtherTest {
    TaskStateOther taskStateOther = new TaskStateOther();

    @Test
    void testUpdateState() {
        Task task = mock(Task.class);
        TaskState currentTaskState = new TaskState(Enums.TaskStateName.QUEUED, null);
        when(task.getTaskState()).thenReturn(currentTaskState);
        TaskState taskState = new TaskState(Enums.TaskStateName.RESERVED, null);

        taskStateOther.updateState(task, taskState);

        verify(task, times(1)).setTaskState(taskState);
        verify(task, times(1)).getTaskState();
        verifyNoMoreInteractions(task);
    }
}
