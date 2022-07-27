package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TasksRetrieverFactoryTest {
    TasksRetrieverFactory factory;
    @Mock
    TasksPool tasksPool;

    @BeforeEach
    void setUp() {
        this.factory = new TasksRetrieverFactory(tasksPool);
    }

    @Test
    void testGetRetriever_returnsRetrieveByAgentAndState_when_agentIdIsPresentAndTaskStateIsPresent() {
        Optional<UUID> agentId = Optional.of(UUID.randomUUID());
        Optional<Enums.TaskStateName> stateName = Optional.of(Enums.TaskStateName.QUEUED);

        TasksRetriever tasksRetriever = factory.getRetriever(agentId, stateName);
        assertEquals(RetrieveByAgentAndState.class, tasksRetriever.getClass());
    }

    @Test
    void testGetRetriever_returnsRetrieveByAgent_when_agentIdIsPresentAndTaskStateIsNotPresent() {
        Optional<UUID> agentId = Optional.of(UUID.randomUUID());
        Optional<Enums.TaskStateName> stateName = Optional.empty();

        TasksRetriever tasksRetriever = factory.getRetriever(agentId, stateName);
        assertEquals(RetrieveByAgent.class, tasksRetriever.getClass());
    }

    @Test
    void testGetRetriever_returnsRetrieveByState_when_agentIdIsNotPresentAndTaskStateIsPresent() {
        Optional<UUID> agentId = Optional.empty();
        Optional<Enums.TaskStateName> stateName = Optional.of(Enums.TaskStateName.QUEUED);

        TasksRetriever tasksRetriever = factory.getRetriever(agentId, stateName);
        assertEquals(RetrieveByState.class, tasksRetriever.getClass());
    }

    @Test
    void testGetRetriever_returnsRetrieveByState_when_agentIdIsNotPresentAndTaskStateIsNotPresent() {
        Optional<UUID> agentId = Optional.empty();
        Optional<Enums.TaskStateName> stateName = Optional.empty();

        TasksRetriever tasksRetriever = factory.getRetriever(agentId, stateName);
        assertEquals(RetrieveAll.class, tasksRetriever.getClass());
    }
}