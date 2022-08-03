package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
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
     * @param taskId         the task id
     * @param requestedState the request state
     * @return the task
     */
    public Task propertyChange(String taskId, TaskState requestedState) {
        logger.info("Task state change listener called");

        Task task = tasksPool.findById(taskId);
        if (task == null) {
            logger.warn("Task not found for id: {}, ignoring request...", taskId);
            return null;
        }

        TaskState currentState = task.getTaskState();

        TaskStateModifier stateModifier = this.factory.getModifier(requestedState.getName());
        stateModifier.updateState(task, requestedState);

        logger.info("Task state changed from {} to {} | Task: {}", currentState, requestedState, task.getId());

        jmsCommunicator.publishTaskStateChangeForReporting(task);

        return task;
    }
}
