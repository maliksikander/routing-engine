package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
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
     * Default constructor. Autowired -> loads the beans.
     *
     * @param tasksPool the tasks pool
     * @param factory   the factory
     */
    @Autowired
    public TaskStateListener(TasksPool tasksPool, TaskStateModifierFactory factory) {
        this.tasksPool = tasksPool;
        this.factory = factory;
    }

    /**
     * Property change task.
     *
     * @param taskId         the task id
     * @param requestedState the request state
     * @return the task
     */
    public Task propertyChange(String taskId, TaskState requestedState) {
        logger.info("Request to Change task:{} state to {} initiated", taskId, requestedState);

        Task task = tasksPool.findById(taskId);
        if (task == null) {
            logger.warn("Task not found for id: {}, ignoring request...", taskId);
            return null;
        }

        TaskState currentState = task.getTaskState();
        logger.info("Task:{} | Current State: {}, Requested state: {}", taskId, currentState, requestedState);

        TaskStateModifier stateModifier = this.factory.getModifier(requestedState.getName());
        boolean isUpdated = stateModifier.updateState(task, requestedState);

        if (isUpdated) {
            logger.info("Task:{} state changed from {} to {}", task.getId(), currentState, requestedState);
        } else {
            logger.info("Could not change Task:{} state from {} to {}", task.getId(), currentState, requestedState);
        }

        return task;
    }
}
