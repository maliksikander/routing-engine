package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.CancelResourceRequest;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.utilities.RestRequest;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type End task service.
 */
@Service
public class CancelResourceServiceImpl implements CancelResourceService {
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
        Task task = tasksPool.findFirstByConversationId(request.getTopicId());
        if (!isProcessable(task)) {
            return;
        }
        taskManager.cancelAgentRequestTtlTimerTask(task.getTopicId());
        task.getTimer().cancel();

        PrecisionQueue precisionQueue = precisionQueuesPool.findById(task.getQueue());
        synchronized (precisionQueue.getServiceQueue()) {
            precisionQueue.removeTask(task);
        }

        if (task.getTaskState().getName().equals(Enums.TaskStateName.QUEUED)) {
            endQueuedTask(task, precisionQueue, request.getReasonCode());
        } else if (task.getTaskState().getName().equals(Enums.TaskStateName.RESERVED)) {
            endReservedTask(task, request.getReasonCode());
        }
    }

    private boolean isProcessable(Task task) {
        if (task == null) {
            return false;
        }
        Enums.TaskStateName state = task.getTaskState().getName();
        if (!(state.equals(Enums.TaskStateName.QUEUED) || state.equals(Enums.TaskStateName.RESERVED))) {
            return false;
        }
        return task.isAgentRequestTimeout();
    }

    private void endQueuedTask(Task task, PrecisionQueue precisionQueue, Enums.TaskStateReasonCode closeReasonCode) {
        task.removePropertyChangeListener(Enums.EventName.TIMER.name(), precisionQueue.getTaskScheduler());
        task.removePropertyChangeListener(Enums.EventName.TASK_REMOVED.name(), precisionQueue.getTaskScheduler());
        removeAndPublish(task, closeReasonCode);
    }

    /**
     * End reserved task.
     *
     * @param task the task
     */
    private void endReservedTask(Task task, Enums.TaskStateReasonCode closeReasonCode) {
        boolean taskRevoked = this.restRequest.postRevokeTask(task.getId(), task.getAssignedTo());
        if (taskRevoked) {
            removeAndPublish(task, closeReasonCode);
            Agent agent = this.agentsPool.findById(task.getAssignedTo());
            if (agent != null) {
                agent.removeReservedTask();
            }
        }
    }

    /**
     * End task.
     *
     * @param task the task
     */
    private void removeAndPublish(Task task, Enums.TaskStateReasonCode closeReasonCode) {
        tasksPool.remove(task);
        tasksRepository.deleteById(task.getId().toString());

        task.setTaskState(new TaskState(Enums.TaskStateName.CLOSED, closeReasonCode));
        jmsCommunicator.publishTaskStateChangeForReporting(task);
    }
}
