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
import java.beans.PropertyChangeEvent;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class TaskStateListenerTest {
    TaskStateListener taskStateListener;
    @Mock
    private TasksPool tasksPool;
    @Mock
    private TaskStateModifierFactory factory;
    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        this.taskStateListener = new TaskStateListener(tasksPool, factory, applicationContext);
    }

    @Test
    void testPropertyChange_when_propertyNameIsNotTaskState() {
        PropertyChangeEvent evt = getPropertyChangeEvent("NotTaskState");
        taskStateListener.propertyChange(evt);

        verifyNoInteractions(tasksPool);
        verifyNoInteractions(factory);
        verifyNoInteractions(applicationContext);
    }

    @Test
    void testPropertyChange_when_taskNotFoundInTasksPool() {
        PropertyChangeEvent evt = getPropertyChangeEvent(Enums.EventName.TASK_STATE.toString());

        when(tasksPool.findById(any())).thenReturn(null);
        taskStateListener.propertyChange(evt);

        verifyNoInteractions(factory);
        verifyNoInteractions(applicationContext);
    }

    @Test
    void testPropertyChange_when_taskFoundInTasksPool() {
        PropertyChangeEvent evt = getPropertyChangeEvent(Enums.EventName.TASK_STATE.toString());
        Task task = mock(Task.class);
        TaskStateModifier taskStateModifier = mock(TaskStateModifier.class);
        JmsCommunicator jmsCommunicator = mock(JmsCommunicator.class);

        when(tasksPool.findById(any())).thenReturn(task);
        when(factory.getModifier(any())).thenReturn(taskStateModifier);
        when(this.applicationContext.getBean(JmsCommunicator.class)).thenReturn(jmsCommunicator);

        taskStateListener.propertyChange(evt);

        verify(taskStateModifier, times(1)).updateState(eq(task), any());
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
    }

    private PropertyChangeEvent getPropertyChangeEvent(String propertyName) {
        return new PropertyChangeEvent(this, propertyName, null, getNewRequest());
    }

    private TaskStateChangeRequest getNewRequest() {
        TaskState requestedState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        return new TaskStateChangeRequest(UUID.randomUUID(), requestedState);
    }
}