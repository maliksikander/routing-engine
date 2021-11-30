package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Task state listener.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskStateListener implements PropertyChangeListener {
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
    private final ApplicationContext applicationContext;

    /**
     * Default constructor. Autowired -> loads the beans.
     *
     * @param tasksPool the tasks pool
     * @param factory   the factory
     */
    @Autowired
    public TaskStateListener(TasksPool tasksPool, TaskStateModifierFactory factory,
                             ApplicationContext applicationContext) {
        this.tasksPool = tasksPool;
        this.factory = factory;
        this.applicationContext = applicationContext;
    }

    /**
     * Gets jms communicator.
     *
     * @return the jms communicator
     */
    @Lookup
    public JmsCommunicator getJmsCommunicator() {
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.debug(Constants.METHOD_STARTED);
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.TASK_STATE.toString())) {
            TaskStateChangeRequest request = (TaskStateChangeRequest) evt.getNewValue();
            logger.info("Task state change requested | Task: {}", request.getTaskId());

            Task task = tasksPool.findById(request.getTaskId());
            if (task == null) {
                logger.warn("Task not found for id: {}, ignoring request...", request.getTaskId());
                return;
            }

            TaskState currentState = task.getTaskState();
            TaskState requestedState = request.getState();

            TaskStateModifier stateModifier = this.factory.getModifier(requestedState.getName());
            stateModifier.updateState(task, request.getState());

            logger.info("Task state changed from {} to {} | Task: {}", currentState, requestedState, task.getId());

            JmsCommunicator jmsCommunicator = this.applicationContext.getBean(JmsCommunicator.class);
            jmsCommunicator.publishTaskStateChangeForReporting(task);
        }
        logger.debug(Constants.METHOD_ENDED);
    }
}
