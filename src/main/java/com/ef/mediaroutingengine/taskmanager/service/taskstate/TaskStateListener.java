package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
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

    private final TasksRepository tasksRepository;
    /**
     * The Factory.
     */
    private final TaskStateModifierFactory factory;

    /**
     * Instantiates a new Task state listener.
     *
     * @param tasksRepository the tasks repository
     * @param factory         the factory
     */
    @Autowired
    public TaskStateListener(TasksRepository tasksRepository, TaskStateModifierFactory factory) {
        this.tasksRepository = tasksRepository;
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

        Task task = tasksRepository.find(taskId);
        if (task == null) {
            logger.warn("Task not found for id: {}, ignoring request...", taskId);
            return null;
        }

        TaskState currentState = task.getState();
        logger.info("Task:{} | Current State: {}, Requested state: {}", taskId, currentState, requestedState);

        if (currentState.getName().equals(requestedState.getName())) {
            logger.info("States are same, could not change Task:{} state from {} to {}", task.getId(), currentState,
                    requestedState);
            return task;
        }

        TaskStateModifier stateModifier = this.factory.getModifier(requestedState.getName());
        if (stateModifier == null) {
            logger.info("Task State Change from: {} to {} not allowed",
                    currentState.getName(), requestedState.getName());
            return task;
        }

        boolean isUpdated = stateModifier.updateState(task, requestedState);

        if (isUpdated) {
            logger.info("Task:{} state changed from {} to {}", task.getId(), currentState, requestedState);
        } else {
            logger.info("Could not change Task:{} state from {} to {}", task.getId(), currentState, requestedState);
        }

        return task;
    }
}