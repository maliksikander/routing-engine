package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.dto.RevokeResourceDto;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.dto.CancelResourceRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type End task service.
 */
@Service
public class CancelResourceServiceImpl implements CancelResourceService {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CancelResourceServiceImpl.class);
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new End task service.
     *
     * @param tasksPool           the tasks pool
     * @param taskManager         the task manager
     * @param precisionQueuesPool the precision queues pool
     * @param agentsPool          the agents pool
     * @param restRequest         the rest request
     */
    @Autowired
    public CancelResourceServiceImpl(TasksPool tasksPool, TaskManager taskManager,
                                     PrecisionQueuesPool precisionQueuesPool,
                                     AgentsPool agentsPool, RestRequest restRequest, JmsCommunicator jmsCommunicator) {
        this.tasksPool = tasksPool;
        this.taskManager = taskManager;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentsPool = agentsPool;
        this.restRequest = restRequest;
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public void cancelResource(CancelResourceRequest request) {
        logger.info("Cancel resource request initiated for conversation: {}", request.getTopicId());

        synchronized (this.tasksPool) {

            Task task = tasksPool.findInProcessTaskFor(request.getTopicId());

            if (task == null) {
                logger.info("No In-Process task found on this conversation, ignoring request...");
                return;
            }

            task.getTimer().cancel();
            taskManager.cancelAgentRequestTtlTimerTask(task.getTopicId());
            taskManager.removeAgentRequestTtlTimerTask(task.getTopicId());
            logger.debug("Agent-Request-Ttl-timer and Task step timers cancelled | Task: {}", task.getId());

            PrecisionQueue queue = precisionQueuesPool.findById(task.getQueue());

            synchronized (queue.getServiceQueue()) {
                task.markForDeletion();
            }

            if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
                this.endQueuedTask(task, queue, request.getReasonCode());
                logger.info("Queued task {} cancelled successfully on topic: {}", task.getId(), request.getTopicId());
            } else if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
                endReservedTask(task, request.getReasonCode());
                logger.info("Reserved task {} cancelled successfully on topic: {}", task.getId(), request.getTopicId());
            }
        }
    }

    void endQueuedTask(Task task, PrecisionQueue queue, Enums.TaskStateReasonCode closeReasonCode) {
        queue.removeTask(task);
        task.removePropertyChangeListener(Enums.EventName.STEP_TIMEOUT.name(), queue.getTaskScheduler());
        removeAndPublish(task, closeReasonCode);
    }

    /**
     * End reserved task.
     *
     * @param task the task
     */
    void endReservedTask(Task task, Enums.TaskStateReasonCode closeReasonCode) {
        removeAndPublish(task, closeReasonCode);

        Agent agent = this.agentsPool.findById(task.getAssignedTo());

        if (agent != null) {
            agent.removeReservedTask();
        }

        this.jmsCommunicator.publishRevokeTask(task, RevokeResourceDto.createForReservedTask(task.getId(),
                agent.getId(), task.getTopicId()));
        logger.info("REVOKE_RESOURCE published");
    }

    /**
     * End task.
     *
     * @param task the task
     */
    void removeAndPublish(Task task, Enums.TaskStateReasonCode closeReasonCode) {
        task.setTaskState(new TaskState(Enums.TaskStateName.CLOSED, closeReasonCode));

        this.taskManager.removeFromPoolAndRepository(task);
        logger.debug("Task {}, removed from in-memory pool and repository", task.getId());
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);
    }
}
