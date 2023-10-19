package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStateWrapUpTest {
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
    void test_updateState_returnsFalse_whenCurrentStateIsNotActive() {
        TaskState currentState = new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.DONE);
        MediaRoutingDomain mrd = getMrdInstance(UUID.randomUUID().toString());
        Task task = Task.getInstanceFrom(null, mrd, null, currentState, getTaskType(), 1);

        assertFalse(taskStateWrapUp.updateState(task, new TaskState(Enums.TaskStateName.WRAP_UP, null)));

        verifyNoInteractions(tasksRepository);
        verifyNoInteractions(jmsCommunicator);
    }

    @Test
    void test_updateState_returnsTrue_whenCurrentStateIsActive() {
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
        return new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
    }
}
