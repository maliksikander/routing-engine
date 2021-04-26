package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import com.ef.mediaroutingengine.model.TaskService;
import com.ef.mediaroutingengine.services.AgentsPool;
import com.ef.mediaroutingengine.services.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.TaskServiceManager;
import com.fasterxml.jackson.databind.JsonNode;
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
    private final TaskServiceManager taskServiceManager;

    /**
     * Default constructor. Autowired -> loads the beans.
     *
     * @param agentsPool the pool of all agents
     * @param precisionQueuesPool the pool of all precision queues
     * @param taskServiceManager the service that handles tasks flow
     */
    @Autowired
    public TaskStateEvent(AgentsPool agentsPool, PrecisionQueuesPool precisionQueuesPool,
                          TaskServiceManager taskServiceManager) {
        this.agentsPool = agentsPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.taskServiceManager = taskServiceManager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.EventProperties.TASK_STATE.toString())) {
            LOGGER.debug("TaskStateEvent.propertyChange() method started");
            try {
                JsonNode node = (JsonNode) evt.getNewValue();
                String taskId = node.has("TaskId") ? node.get("TaskId").textValue() : "";
                String stateStr = node.has("State") ? node.get("State").textValue() : "";

                CommonEnums.TaskState state = CommonEnums.TaskState.valueOf(stateStr);
                TaskService task = taskServiceManager.getTask(taskId);
                switch (state) {
                    case INTERRUPTED:
                    case OFFERED:
                    case ACCEPTED:
                    case NEW:
                    case PAUSED:
                    case UNKNOWN:
                    case WRAPPING_UP:
                        task.setTaskState(state);
                        break;
                    case ACTIVE:
                        task.setTaskState(state);
                        task.setStartTime(System.currentTimeMillis());
                        break;
                    case CLOSED:
                        this.agentsPool.endTask(task);
                        this.precisionQueuesPool.endTask(task);
                        taskServiceManager.removeTask(taskId);
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
