package com.ef.mediaroutingengine.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.StepEntity;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.TaskRouter;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class StepsServiceImplTest {

    @InjectMocks
    StepsServiceImpl stepsService;
    @Mock
    AgentsPool agentsPool;
    @Mock
    TaskRouter taskRouter;
    PrecisionQueue precisionQueue;
    PrecisionQueueEntity precisionQueueEntity;
    StepEntity stepEntity;
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;

    @BeforeEach
    void setUp() {
        stepEntity = new StepEntity();
        stepEntity.setId("ASDFNSKF32");

        precisionQueueEntity = new PrecisionQueueEntity();
        precisionQueueEntity.setSteps(List.of(stepEntity));
        precisionQueue = new PrecisionQueue(precisionQueueEntity, agentsPool, taskRouter);
    }

    private KeycloakUser getNewKeyClockUser() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("6eb972c2-246e-4e04-bb82-c5b327947745");
        keycloakUser.setUsername("user1");
        return keycloakUser;
    }

    private CCUser getNewCcUser() {
        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(getNewKeyClockUser());
        ccUser.setId(ccUser.getKeycloakUser().getId());
        return ccUser;
    }

    @Nested
    class test_previewAgentsMatchingStepCriteriaInQueue {
        String queueId = "GJSD34";

        @Test
        void shouldReturnListOfAgentsThatMeetTheStepsCriteria_inQueueLevel() {
            precisionQueue.getSteps().get(0).getAssociatedAgents().add(new Agent(getNewCcUser()));
            when(precisionQueuesPool.findById(queueId)).thenReturn(precisionQueue);

            assertEquals(1, stepsService.previewAgentsMatchingStepCriteriaInQueue(queueId, Optional.empty()).size());
            assertEquals(getNewKeyClockUser().getUsername(),
                    stepsService.previewAgentsMatchingStepCriteriaInQueue(queueId,
                            Optional.empty()).stream().findFirst().get().getUsername());
        }

        @Test
        void shouldReturnListOfAgentsThatMeetTheStepsCriteria_inQueueLevel_forSpecificStep_providedAsParam() {
            precisionQueue.getSteps().get(0).getAssociatedAgents().add(new Agent(getNewCcUser()));
            when(precisionQueuesPool.findById(queueId)).thenReturn(precisionQueue);

            assertEquals(1,
                    stepsService.previewAgentsMatchingStepCriteriaInQueue(queueId, Optional.of(stepEntity.getId()))
                            .size());
            assertEquals(getNewKeyClockUser().getUsername(),
                    stepsService.previewAgentsMatchingStepCriteriaInQueue(queueId,
                            Optional.of(stepEntity.getId())).stream().findFirst().get().getUsername());
        }

        @Test
        void shouldReturn_404_NotFound_WhenQueueId_passedAsParamIsNotFound() {
            when(precisionQueuesPool.findById(queueId)).thenReturn(null);
            assertThrows(NotFoundException.class,
                    () -> stepsService.previewAgentsMatchingStepCriteriaInQueue(queueId, Optional.empty()));
        }

    }
}