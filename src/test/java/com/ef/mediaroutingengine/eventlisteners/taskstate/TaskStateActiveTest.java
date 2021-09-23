package com.ef.mediaroutingengine.eventlisteners.taskstate;

import static org.mockito.Mockito.mock;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
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
    void test_updateState() {
        Task task = mock(Task.class);
        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        Agent agent = getAgent();

    }

    private Agent getAgent() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        return new Agent(ccUser);
    }
}