package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.model.Task;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStateListener.class);
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
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.TASK_STATE.toString())) {
            LOGGER.debug("TaskStateEvent.propertyChange() method started");
            TaskStateChangeRequest request = (TaskStateChangeRequest) evt.getNewValue();
            Task task = tasksPool.findById(request.getTaskId());
            if (task == null) {
                return;
            }
            TaskStateModifier stateModifier = this.factory.getModifier(request.getState().getName());
            stateModifier.updateState(task, request.getState());

            JmsCommunicator jmsCommunicator = this.applicationContext.getBean(JmsCommunicator.class);
            jmsCommunicator.publishTaskStateChangeForReporting(task);
            LOGGER.debug("TaskStateEvent.propertyChange() end");
        }
    }
}
