package com.ef.mediaroutingengine.taskmanager.service;

import com.ef.mediaroutingengine.global.locks.ConversationLock;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class TasksServiceTest {

    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private TasksRepository tasksRepository;
    @Mock
    private MrdPool mrdPool;
    @Mock
    private RestRequest restRequest;
    @Mock
    private TaskManager taskManager;
    @Mock
    private ConversationLock conversationLock;
    private TasksService tasksService;

    @BeforeEach
    void setup() {
        this.tasksService = new TasksService(precisionQueuesPool, tasksRepository, mrdPool, restRequest, taskManager);
    }

    @Nested
    @DisplayName("Calculate EWT method tests")
    class TestCalculateEwt {
        @Test
        void when_totalAssociatedAgentsAreZero_ewtMaxValueIsNull_then_ewtShouldBeIntMaxValue() {
            // given
            int taskPosition = 1;
            int totalAgents = 0;
            int averageHandleTime = 10;
            Integer ewtMinValue = 5;
            Integer ewtMaxValue = null;

            // when
            int ewt = tasksService.calculateEwt(taskPosition, totalAgents, averageHandleTime, ewtMinValue, ewtMaxValue);

            //then
            assertEquals(Integer.MAX_VALUE, ewt);
        }

        @Test
        void when_totalAssociatedAgentsAreZero_ewtMaxValueIsNotNull_then_ewtShouldBeEwtMaxValue() {
            // given
            int taskPosition = 1;
            int totalAgents = 0;
            int averageHandleTime = 10;
            Integer ewtMinValue = 5;
            Integer ewtMaxValue = 10;

            // when
            int ewt = tasksService.calculateEwt(taskPosition, totalAgents, averageHandleTime, ewtMinValue, ewtMaxValue);

            //then
            assertEquals(ewtMaxValue, ewt);
        }

        @Test
        void when_ewtIsLessThanEwtMinValueAndEwtMinValueIsNotNull_then_ewtShouldBeEwtMinValue() {
            // given
            int taskPosition = 1;
            int totalAgents = 2;
            int averageHandleTime = 10;
            Integer ewtMinValue = 7;
            Integer ewtMaxValue = 10;

            // when
            int ewt = tasksService.calculateEwt(taskPosition, totalAgents, averageHandleTime, ewtMinValue, ewtMaxValue);

            //then
            assertEquals(ewtMinValue, ewt);
        }

        @Test
        void when_ewtIsLessThanEwtMinValueAndEwtMinValueIsNull_then_ewtShouldNotBeEwtMinValue() {
            // given
            int taskPosition = 1;
            int totalAgents = 2;
            int averageHandleTime = 10;
            Integer ewtMinValue = null;
            Integer ewtMaxValue = 10;

            // when
            int ewt = tasksService.calculateEwt(taskPosition, totalAgents, averageHandleTime, ewtMinValue, ewtMaxValue);

            //then
            assertNotEquals(ewtMinValue, ewt);
        }

        @Test
        void when_ewtIsGreaterThanEwtMaxValueAndEwtMaxValueIsNotNull_then_ewtShouldBeEwtMaxValue() {
            // given
            int taskPosition = 1;
            int totalAgents = 2;
            int averageHandleTime = 20;
            Integer ewtMinValue = 2;
            Integer ewtMaxValue = 5;

            // when
            int ewt = tasksService.calculateEwt(taskPosition, totalAgents, averageHandleTime, ewtMinValue, ewtMaxValue);

            //then
            assertEquals(ewtMaxValue, ewt);
        }

        @Test
        void when_ewtIsGreaterThanEwtMaxValueAndEwtMaxValueIsNull_then_ewtShouldNotBeEwtMaxValue() {
            // given
            int taskPosition = 1;
            int totalAgents = 2;
            int averageHandleTime = 10;
            Integer ewtMinValue = 2;
            Integer ewtMaxValue = null;

            // when
            int ewt = tasksService.calculateEwt(taskPosition, totalAgents, averageHandleTime, ewtMinValue, ewtMaxValue);

            //then
            assertNotEquals(ewtMaxValue, ewt);
        }
    }
}