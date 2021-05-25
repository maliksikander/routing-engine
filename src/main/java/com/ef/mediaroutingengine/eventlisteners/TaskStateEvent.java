package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.AgentsPool;
import com.ef.mediaroutingengine.services.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.TasksPool;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskStateEvent implements PropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStateEvent.class);
    private final AgentsPool agentsPool;
    private final PrecisionQueuesPool precisionQueuesPool;
    private final TasksPool tasksPool;

    /**
     * Default constructor. Autowired -> loads the beans.
     *
     * @param agentsPool          the pool of all agents
     * @param precisionQueuesPool the pool of all precision queues
     * @param tasksPool           the service that handles tasks flow
     */
    @Autowired
    public TaskStateEvent(AgentsPool agentsPool, PrecisionQueuesPool precisionQueuesPool,
                          TasksPool tasksPool) {
        this.agentsPool = agentsPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.tasksPool = tasksPool;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(Enums.EventName.TASK_STATE.toString())) {
            LOGGER.debug("TaskStateEvent.propertyChange() method started");
            try {
                TaskDto taskDto = (TaskDto) evt.getNewValue();
                Task task = tasksPool.getTask(taskDto.getId());
                Enums.TaskState state = taskDto.getState();

                switch (state) {
                    case CREATED:
                    case QUEUED:
                    case RESERVED:
                    case PAUSED:
                    case WRAP_UP:
                        task.setTaskState(state);
                        break;
                    case ACTIVE:
                        task.setTaskState(state);
                        task.setStartTime(System.currentTimeMillis());
                        break;
                    case CLOSED:
                        this.agentsPool.endTask(task);
                        this.precisionQueuesPool.endTask(task);
                        tasksPool.removeTask(taskDto.getId().toString());
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                LOGGER.debug("Exception occurred in TaskStateEvent.propertyChange(). Message: {}", ex.getMessage());
            }
            LOGGER.debug("TaskStateEvent.propertyChange() end");
        }
    }
}