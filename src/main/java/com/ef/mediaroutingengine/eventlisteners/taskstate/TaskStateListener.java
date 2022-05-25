package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Task state listener.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskStateListener {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskStateListener.class);
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Factory.
     */
    private final TaskStateModifierFactory factory;
    /**
     * The Application context.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Default constructor. Autowired -> loads the beans.
     *
     * @param tasksPool       the tasks pool
     * @param factory         the factory
     * @param jmsCommunicator the jms communicator
     */
    @Autowired
    public TaskStateListener(TasksPool tasksPool, TaskStateModifierFactory factory,
                             JmsCommunicator jmsCommunicator) {
        this.tasksPool = tasksPool;
        this.factory = factory;
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Property change task.
     *
     * @param request the request
     * @return the task
     */
    public Task propertyChange(TaskStateChangeRequest request) {
        logger.info("Task state change listener called");

        Task task = tasksPool.findById(request.getTaskId());
        if (task == null) {
            logger.warn("Task not found for id: {}, ignoring request...", request.getTaskId());
            return null;
        }

        TaskState currentState = task.getTaskState();
        TaskState requestedState = request.getState();

        TaskStateModifier stateModifier = this.factory.getModifier(requestedState.getName());
        stateModifier.updateState(task, request.getState());

        logger.info("Task state changed from {} to {} | Task: {}", currentState, requestedState, task.getId());

        jmsCommunicator.publishTaskStateChangeForReporting(task);

        return task;
    }
}
