package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.dto.QueueAgentStats;
import com.ef.mediaroutingengine.dto.QueueStatsDto;
import com.ef.mediaroutingengine.dto.QueueTaskStats;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Queue stats service.
 */
@Service
public class QueueStatsServiceImpl implements QueueStatsService {
    private final Logger logger = LoggerFactory.getLogger(QueueStatsServiceImpl.class);

    private final PrecisionQueuesPool precisionQueuesPool;
    private final TasksPool tasksPool;

    @Autowired
    public QueueStatsServiceImpl(PrecisionQueuesPool precisionQueuesPool,
                                 TasksPool tasksPool) {
        this.precisionQueuesPool = precisionQueuesPool;
        this.tasksPool = tasksPool;
    }

    @Override
    public QueueStatsDto getQueueStats(String queueId) {
        logger.info("Request to get queue-stats-summary for queue: {} initiated", queueId);
        PrecisionQueue queue = this.precisionQueuesPool.findById(queueId);

        if (queue == null) {
            String errorMessage = "Queue with id: " + queueId + " not found";
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        QueueStatsDto queueStatsDto = this.getQueueStateFor(queue);

        logger.info("Successfully generated queue-stats-summary for queue: {}", queueId);
        return queueStatsDto;
    }

    @Override
    public List<QueueStatsDto> getQueueStatsForAll() {
        logger.info("Request to get queue-stats-summary for all queues initiated");
        List<QueueStatsDto> res = new ArrayList<>();

        for (PrecisionQueue queue : this.precisionQueuesPool.toList()) {
            QueueStatsDto queueStatsDto = this.getQueueStateFor(queue);
            res.add(queueStatsDto);
            logger.debug("Successfully generated queue-stats-summary for queue: {}", queue.getId());
        }

        logger.info("Successfully generated queue-stats-summary for all queues");
        return res;
    }

    QueueStatsDto getQueueStateFor(PrecisionQueue queue) {
        QueueTaskStats queueTaskStats = this.getQueueTaskStats(queue.getId());
        logger.debug("Successfully generated Tasks stats Summary for queue: {}", queue.getId());
        QueueAgentStats queueAgentStats = this.getQueueAgentStats(queue);
        logger.debug("Successfully generated Agents stats Summary for queue: {}", queue.getId());
        return new QueueStatsDto(queue.getId(), queue.getName(), queueTaskStats, queueAgentStats);
    }

    QueueTaskStats getQueueTaskStats(String queueId) {
        QueueTaskStats queueTaskStats = new QueueTaskStats();

        for (Task task : this.tasksPool.findByQueueId(queueId)) {
            if (task.getTaskState().getName().equals(Enums.TaskStateName.ACTIVE)) {
                queueTaskStats.incrTotalActive();
            } else if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                queueTaskStats.incrTotalQueued();
                if (task.getEnqueueTime() < queueTaskStats.getLongestInQueue()) {
                    queueTaskStats.setLongestInQueue(task.getEnqueueTime());
                }
            }
        }

        if (queueTaskStats.getTotalQueued() == 0) {
            queueTaskStats.setLongestInQueue(-1L);
        }

        return queueTaskStats;
    }

    QueueAgentStats getQueueAgentStats(PrecisionQueue queue) {
        MediaRoutingDomain mrd = queue.getMrd();
        QueueAgentStats queueAgentStats = new QueueAgentStats(mrd.getName());

        for (Agent agent : queue.getAllAssociatedAgents()) {
            Enums.AgentMrdStateName mrdState = agent.getAgentMrdState(mrd.getId()).getState();

            if (mrdState.equals(Enums.AgentMrdStateName.NOT_READY)) {
                queueAgentStats.incrNotReadyAgents();
            } else if (mrdState.equals(Enums.AgentMrdStateName.READY)) {
                queueAgentStats.incrReadyAgents();
            } else if (mrdState.equals(Enums.AgentMrdStateName.ACTIVE)) {
                queueAgentStats.incrActiveAgents();
            } else if (mrdState.equals(Enums.AgentMrdStateName.PENDING_NOT_READY)) {
                queueAgentStats.incrPendingNotReadyAgents();
            } else if (mrdState.equals(Enums.AgentMrdStateName.BUSY)) {
                queueAgentStats.incrBusyAgents();
            }
        }

        return queueAgentStats;
    }
}
