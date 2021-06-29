package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskStateListener implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStateListener.class);
    private final TasksPool tasksPool;
    private final TaskStateModifierFactory factory;

    /**
     * Default constructor. Autowired -> loads the beans.
     *
     * @param tasksPool the service that handles tasks flow
     */
    @Autowired
    public TaskStateListener(TasksPool tasksPool, TaskStateModifierFactory factory) {
        this.tasksPool = tasksPool;
        this.factory = factory;
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

            LOGGER.debug("TaskStateEvent.propertyChange() end");
        }
    }
}
