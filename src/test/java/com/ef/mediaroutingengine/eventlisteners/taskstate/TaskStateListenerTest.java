package com.ef.mediaroutingengine.eventlisteners.taskstate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStateListenerTest {
    TaskStateListener taskStateListener;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private TaskStateModifierFactory factory;
    @Mock
    private JmsCommunicator jmsCommunicator;

    @BeforeEach
    void setUp() {
        this.taskStateListener = new TaskStateListener(tasksPool, factory, jmsCommunicator);
    }

    @Test
    void testPropertyChange_when_taskNotFoundInTasksPool() {
        when(tasksPool.findById(any())).thenReturn(null);
        taskStateListener.propertyChange(getNewRequest());

        verifyNoInteractions(factory);
        verifyNoInteractions(jmsCommunicator);
    }

    @Test
    void testPropertyChange_when_taskFoundInTasksPool() {
        Task task = mock(Task.class);
        TaskStateModifier taskStateModifier = mock(TaskStateModifier.class);

        when(tasksPool.findById(any())).thenReturn(task);
        when(factory.getModifier(any())).thenReturn(taskStateModifier);

        taskStateListener.propertyChange(getNewRequest());

        verify(taskStateModifier, times(1)).updateState(eq(task), any());
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
    }

    private TaskStateChangeRequest getNewRequest() {
        TaskState requestedState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        return new TaskStateChangeRequest(UUID.randomUUID(), requestedState);
    }
}