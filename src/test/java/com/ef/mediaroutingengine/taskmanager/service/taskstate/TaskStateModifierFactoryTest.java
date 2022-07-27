package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStateModifierFactoryTest {
    private TaskStateModifierFactory factory;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private AgentsPool agentsPool;
    @Mock
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        this.factory = new TaskStateModifierFactory(tasksRepository, precisionQueuesPool, agentsPool, taskManager);
    }

    @Test
    void testGetModifier_returnsTaskStateCloseModifier_when_requestedStateIsClosed() {
        assertEquals(TaskStateClose.class, factory.getModifier(Enums.TaskStateName.CLOSED).getClass());
    }

    @Test
    void testGetModifier_returnsTaskStateActiveModifier_when_requestedStateIsActive() {
        assertEquals(TaskStateActive.class, factory.getModifier(Enums.TaskStateName.ACTIVE).getClass());
    }

    @Test
    void testGetModifier_returnsTaskStateOtherModifier_when_requestedStateIsNot_ClosedOrActive() {
        assertEquals(TaskStateOther.class, factory.getModifier(Enums.TaskStateName.QUEUED).getClass());
        assertEquals(TaskStateOther.class, factory.getModifier(Enums.TaskStateName.RESERVED).getClass());
    }
}