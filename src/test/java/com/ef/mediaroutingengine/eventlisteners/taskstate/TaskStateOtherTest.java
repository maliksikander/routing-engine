package com.ef.mediaroutingengine.eventlisteners.taskstate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import org.junit.jupiter.api.Test;

class TaskStateOtherTest {
    TaskStateOther taskStateOther = new TaskStateOther();

    @Test
    void testUpdateState() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.RESERVED, null);

        taskStateOther.updateState(task, taskState);

        verify(task, times(1)).setTaskState(taskState);
        verifyNoMoreInteractions(task);
    }
}
