package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
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
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
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
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;

    /**
     * Instantiates a new End task service.
     *
     * @param tasksPool           the tasks pool
     * @param taskManager         the task manager
     * @param precisionQueuesPool the precision queues pool
     * @param jmsCommunicator     the jms communicator
     * @param tasksRepository     the tasks repository
     * @param agentsPool          the agents pool
     * @param restRequest         the rest request
     */
    @Autowired
    public CancelResourceServiceImpl(TasksPool tasksPool, TaskManager taskManager,
                                     PrecisionQueuesPool precisionQueuesPool, JmsCommunicator jmsCommunicator,
                                     TasksRepository tasksRepository, AgentsPool agentsPool, RestRequest restRequest) {
        this.tasksPool = tasksPool;
        this.taskManager = taskManager;
        this.precisionQueuesPool = precisionQueuesPool;
        this.jmsCommunicator = jmsCommunicator;
        this.tasksRepository = tasksRepository;
        this.agentsPool = agentsPool;
        this.restRequest = restRequest;
    }

    @Override
    public void cancelResource(CancelResourceRequest request) {
        logger.info("Cancel resource request initiated | topic: {}", request.getTopicId());

        Task task = tasksPool.findInProcessTaskFor(request.getTopicId());
        if (!isProcessable(task)) {
            return;
        }
        logger.debug("Task {} is processable", task.getId());

        taskManager.cancelAgentRequestTtlTimerTask(task.getTopicId());
        taskManager.removeAgentRequestTtlTimerTask(task.getTopicId());
        logger.debug("Agent-Request-Ttl-timer-task cancelled and removed | Task: {}", task.getId());

        task.getTimer().cancel();
        logger.debug("Task {} step timer cancelled", task.getId());

        PrecisionQueue precisionQueue = precisionQueuesPool.findById(task.getQueue());
        synchronized (precisionQueue.getServiceQueue()) {
            precisionQueue.removeTask(task);
            logger.debug("Task {} removed from queue", task.getId());
        }

        if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
            endQueuedTask(task, precisionQueue, request.getReasonCode());
            logger.info("Queued task {} cancelled successfully on topic: {}", task.getId(), request.getTopicId());
        } else if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
            endReservedTask(task, request.getReasonCode());
            logger.info("Reserved task {} cancelled successfully on topic: {}", task.getId(), request.getTopicId());
        }
    }

    boolean isProcessable(Task task) {
        if (task == null) {
            logger.info("No Task found on this topic, ignoring request");
            return false;
        }
        return !task.isMarkedForDeletion();
    }

    void endQueuedTask(Task task, PrecisionQueue precisionQueue, Enums.TaskStateReasonCode closeReasonCode) {
        task.removePropertyChangeListener(Enums.EventName.STEP_TIMEOUT.name(), precisionQueue.getTaskScheduler());
        removeAndPublish(task, closeReasonCode);
    }

    /**
     * End reserved task.
     *
     * @param task the task
     */
    void endReservedTask(Task task, Enums.TaskStateReasonCode closeReasonCode) {
        boolean taskRevoked = this.restRequest.postRevokeTask(task);
        if (taskRevoked) {
            removeAndPublish(task, closeReasonCode);
            Agent agent = this.agentsPool.findById(task.getAssignedTo());
            if (agent != null) {
                agent.removeReservedTask();
            }
        } else {
            task.markForDeletion(closeReasonCode);
            logger.info("Revoke-task API did not return 200 response, marked task: {} for deletion", task.getId());
        }
    }

    /**
     * End task.
     *
     * @param task the task
     */
    void removeAndPublish(Task task, Enums.TaskStateReasonCode closeReasonCode) {
        tasksPool.remove(task);
        tasksRepository.deleteById(task.getId().toString());
        logger.debug("Task {}, removed from in-memory pool and repository", task.getId());

        task.setTaskState(new TaskState(Enums.TaskStateName.CLOSED, closeReasonCode));
        jmsCommunicator.publishTaskStateChangeForReporting(task);
    }
}
