package com.ef.mediaroutingengine.eventlisteners.taskstate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
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