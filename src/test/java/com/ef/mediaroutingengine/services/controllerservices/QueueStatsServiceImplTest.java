package com.ef.mediaroutingengine.services.controllerservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.QueueAgentStats;
import com.ef.mediaroutingengine.dto.QueueStatsDto;
import com.ef.mediaroutingengine.dto.QueueTaskStats;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueueStatsServiceImplTest {
    @Mock
    private PrecisionQueuesPool precisionQueuesPool;
    @Mock
    private TasksPool tasksPool;

    private QueueStatsServiceImpl queueStatsService;

    @BeforeEach
    void setUp() {
        this.queueStatsService = new QueueStatsServiceImpl(precisionQueuesPool, tasksPool);
    }

    @Test
    void test_getQueueStats_throwsNotFoundException_when_queueNotFound_forRequestingQueueId() {
        String queueId = UUID.randomUUID().toString();
        when(precisionQueuesPool.findById(queueId)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> this.queueStatsService.getQueueStats(queueId));
    }

    @Test
    void test_getQueueStats_returnsQueueStats_when_queueFound() {
        String queueId = UUID.randomUUID().toString();
        PrecisionQueue queue = mock(PrecisionQueue.class);
        QueueStatsServiceImpl spy = Mockito.spy(this.queueStatsService);

        QueueStatsDto queueStatsDto = new QueueStatsDto();
        when(precisionQueuesPool.findById(queueId)).thenReturn(queue);
        doReturn(queueStatsDto).when(spy).getQueueStateFor(queue);

        QueueStatsDto res = spy.getQueueStats(queueId);

        assertEquals(queueStatsDto, res);
    }

    @Test
    void test_getQueueStatsForAll() {
        List<PrecisionQueue> precisionQueueList = new ArrayList<>();
        precisionQueueList.add(mock(PrecisionQueue.class));
        precisionQueueList.add(mock(PrecisionQueue.class));

        QueueStatsServiceImpl spy = Mockito.spy(this.queueStatsService);

        when(precisionQueuesPool.toList()).thenReturn(precisionQueueList);

        for (PrecisionQueue queue: precisionQueueList) {
            doReturn(mock(QueueStatsDto.class)).when(spy).getQueueStateFor(queue);
        }

        List<QueueStatsDto> res = spy.getQueueStatsForAll();

        assertEquals(precisionQueueList.size(), res.size());
    }

    @Test
    void test_getQueueStateFor() {
        PrecisionQueue queue = mock(PrecisionQueue.class);
        String queueId = UUID.randomUUID().toString();
        QueueStatsServiceImpl spy = Mockito.spy(this.queueStatsService);

        QueueTaskStats queueTaskStats = new QueueTaskStats();
        long longestInQueue = System.currentTimeMillis();
        queueTaskStats.setLongestInQueue(longestInQueue);
        queueTaskStats.incrTotalQueued();

        QueueAgentStats queueAgentStats = new QueueAgentStats("mrd1");
        queueAgentStats.incrNotReadyAgents();
        queueAgentStats.incrNotReadyAgents();
        queueAgentStats.incrActiveAgents();

        when(queue.getId()).thenReturn(queueId);
        doReturn(queueTaskStats).when(spy).getQueueTaskStats(queueId);
        doReturn(queueAgentStats).when(spy).getQueueAgentStats(queue);
        when(queue.getName()).thenReturn("queue1");

        QueueStatsDto queueStatsDto = spy.getQueueStateFor(queue);

        assertEquals("queue1", queueStatsDto.getName());

        assertEquals(1, queueStatsDto.getTotalQueued());
        assertEquals(longestInQueue, queueStatsDto.getLongestInQueue());
        assertEquals(0, queueStatsDto.getTotalActive());

        assertEquals("mrd1", queueStatsDto.getMrdName());
        assertEquals(2, queueStatsDto.getNotReadyAgents());
        assertEquals(0, queueStatsDto.getReadyAgents());
        assertEquals(1, queueStatsDto.getActiveAgents());
        assertEquals(0, queueStatsDto.getPendingNotReadyAgents());
        assertEquals(0, queueStatsDto.getBusyAgents());
    }

    @Test
    void test_getQueueTaskStats_in_happyScenario() {
        String queueId = UUID.randomUUID().toString();

        List<Task> queueTasks = new ArrayList<>();
        queueTasks.add(this.getTaskInstance(new TaskState(Enums.TaskStateName.QUEUED, null)));
        queueTasks.add(this.getTaskInstance(new TaskState(Enums.TaskStateName.QUEUED, null)));
        queueTasks.add(this.getTaskInstance(new TaskState(Enums.TaskStateName.ACTIVE, null)));
        queueTasks.add(this.getTaskInstance(new TaskState(Enums.TaskStateName.ACTIVE, null)));
        queueTasks.add(this.getTaskInstance(new TaskState(Enums.TaskStateName.RESERVED, null)));

        when(tasksPool.findByQueueId(queueId)).thenReturn(queueTasks);

        QueueTaskStats queueTaskStats = this.queueStatsService.getQueueTaskStats(queueId);

        assertEquals(2, queueTaskStats.getTotalQueued());
        assertEquals(2, queueTaskStats.getTotalActive());

        Task oldestTaskInQueue = queueTasks.get(0);
        assertEquals(oldestTaskInQueue.getEnqueueTime(), queueTaskStats.getLongestInQueue());
    }

    @Test
    void test_getQueueTaskStats_returnsLongestInQueue_equalsMinusOne_when_noQueuedTasksPresent() {
        String queueId = UUID.randomUUID().toString();

        List<Task> queueTasks = new ArrayList<>();
        queueTasks.add(this.getTaskInstance(new TaskState(Enums.TaskStateName.ACTIVE, null)));
        queueTasks.add(this.getTaskInstance(new TaskState(Enums.TaskStateName.RESERVED, null)));

        when(tasksPool.findByQueueId(queueId)).thenReturn(queueTasks);

        QueueTaskStats queueTaskStats = this.queueStatsService.getQueueTaskStats(queueId);

        assertEquals(0, queueTaskStats.getTotalQueued());
        assertEquals(1, queueTaskStats.getTotalActive());
        assertEquals(-1, queueTaskStats.getLongestInQueue());
    }

    @Test
    void test_getQueueAgentStats() {
        PrecisionQueue queue = mock(PrecisionQueue.class);
        MediaRoutingDomain mrd = this.getMrdInstance("dummyMrd");

        List<Agent> agents = new ArrayList<>();
        agents.add(this.getAgentInstance("a1", mrd, Enums.AgentMrdStateName.NOT_READY));
        agents.add(this.getAgentInstance("a2", mrd, Enums.AgentMrdStateName.READY));
        agents.add(this.getAgentInstance("a3", mrd, Enums.AgentMrdStateName.ACTIVE));
        agents.add(this.getAgentInstance("a4", mrd, Enums.AgentMrdStateName.PENDING_NOT_READY));
        agents.add(this.getAgentInstance("a5", mrd, Enums.AgentMrdStateName.BUSY));

        when(queue.getMrd()).thenReturn(mrd);
        when(queue.getAllAssociatedAgents()).thenReturn(agents);

        QueueAgentStats queueAgentStats = this.queueStatsService.getQueueAgentStats(queue);

        assertEquals(mrd.getName(), queueAgentStats.getMrdName());
        assertEquals(1, queueAgentStats.getNotReadyAgents());
        assertEquals(1, queueAgentStats.getReadyAgents());
        assertEquals(1, queueAgentStats.getActiveAgents());
        assertEquals(1, queueAgentStats.getPendingNotReadyAgents());
        assertEquals(1, queueAgentStats.getBusyAgents());
    }

    private MediaRoutingDomain getMrdInstance(String name) {
        MediaRoutingDomain mrd = new MediaRoutingDomain();
        mrd.setId(UUID.randomUUID().toString());
        mrd.setName(name);
        return mrd;
    }

    private Agent getAgentInstance(String name, MediaRoutingDomain mrd, Enums.AgentMrdStateName state) {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        keycloakUser.setUsername(name);

        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.setId(keycloakUser.getId());

        Agent agent = new Agent(ccUser);
        agent.addAgentMrdState(new AgentMrdState(mrd,state));
        return agent;
    }

    private Task getTaskInstance(TaskState state) {
        ChannelSession channelSession = new ChannelSession();
        MediaRoutingDomain mrd = this.getMrdInstance("dummy");
        String queueId = UUID.randomUUID().toString();

        return Task.getInstanceFrom(channelSession, mrd, queueId, state);
    }
}