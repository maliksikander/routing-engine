package com.ef.mediaroutingengine.eventlisteners.taskstate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStateActiveTest {
    private TaskStateActive taskStateActive;
    @Mock
    private TaskManager taskManager;
    @Mock
    private AgentsPool agentsPool;

    @BeforeEach
    void setUp() {
        this.taskStateActive = new TaskStateActive(taskManager, agentsPool);
    }

    @Test
    void test_updateState_when_agentIsNull() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);

        when(task.getAssignedTo()).thenReturn(UUID.randomUUID());
        when(agentsPool.findById(any())).thenReturn(null);

        taskStateActive.updateState(task, taskState);

        verifyNoMoreInteractions(task);
        verifyNoInteractions(taskManager);
    }

    @Test
    void test_updateState_when_agentIsNotNull() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        Agent agent = mock(Agent.class);
        UUID topicId = UUID.randomUUID();
        MediaRoutingDomain mrd = getNewMrd();

        when(task.getAssignedTo()).thenReturn(UUID.randomUUID());
        when(agentsPool.findById(any())).thenReturn(agent);
        when(task.getTopicId()).thenReturn(topicId).thenReturn(topicId);
        when(task.getMrd()).thenReturn(mrd);
        when(task.getRoutingMode()).thenReturn(RoutingMode.PUSH);

        taskStateActive.updateState(task, taskState);

        verify(task, times(1)).setTaskState(taskState);
        verify(task, times(1)).setStartTime(anyLong());
        verify(taskManager, times(1)).cancelAgentRequestTtlTimerTask(topicId);
        verify(taskManager, times(1)).removeAgentRequestTtlTimerTask(topicId);
        verify(agent, times(1)).assignPushTask(task);
        verify(taskManager, times(1)).updateAgentMrdState(agent, mrd.getId());

        verifyNoMoreInteractions(task);
        verifyNoMoreInteractions(taskManager);
        verifyNoMoreInteractions(agent);

    }

    private MediaRoutingDomain getNewMrd() {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName("Chat");
        mrd.setDescription("Description");
        return mrd;
    }
}