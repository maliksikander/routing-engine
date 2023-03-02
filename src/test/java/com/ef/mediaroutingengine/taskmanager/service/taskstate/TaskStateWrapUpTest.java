package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskStateWrapUpTest {
    private TaskStateWrapUp taskStateWrapUp;
    @Mock
    private JmsCommunicator jmsCommunicator;
    @Mock
    private TasksRepository tasksRepository;

    @BeforeEach
    void setUp() {
        this.taskStateWrapUp = new TaskStateWrapUp(tasksRepository, jmsCommunicator);
    }

    @Test
    void test_updateToWrapUp_whenTaskIsNotActive() {
        String mrdId = UUID.randomUUID().toString();
        String error = "Task is not active, could not change the task state to wrap up.";

        TaskState currentState = new TaskState();
        currentState.setName(Enums.TaskStateName.CLOSED);
        MediaRoutingDomain mrd = getMrdInstance(mrdId);
        Task task = Task.getInstanceFrom(null, mrd, null, currentState, getTaskType());
        TaskState newState = new TaskState();
        newState.setName(Enums.TaskStateName.WRAP_UP);


        assertThatThrownBy(() -> taskStateWrapUp.updateState(task, newState)).isInstanceOf(IllegalStateException.class)
                .hasMessage(error);

        verifyNoInteractions(tasksRepository);
        verifyNoInteractions(jmsCommunicator);

    }

    @Test
    void test_updateToWrapUp_successfully() {
        Task task = mock(Task.class);
        TaskState currentState = new TaskState();
        currentState.setName(Enums.TaskStateName.ACTIVE);

        TaskState newState = new TaskState();
        newState.setName(Enums.TaskStateName.WRAP_UP);


        when(task.getTaskState()).thenReturn(currentState);
        taskStateWrapUp.updateState(task, newState);

        verify(task, times(1)).setTaskState(newState);
        verify(tasksRepository, times(1)).save(eq(task.getId()), any());
        verify(jmsCommunicator, times(1)).publishTaskStateChangeForReporting(task);
    }

    protected MediaRoutingDomain getMrdInstance(String mrdId) {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(mrdId);
        mrd.setName("MRD");
        mrd.setDescription("MRD Desc");
        mrd.setMaxRequests(5);
        return mrd;
    }

    protected TaskType getTaskType() {
        TaskType taskType = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
        return taskType;
    }
}
