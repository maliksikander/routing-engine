package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.utilities.AdapterUtility;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Task state active.
 */
@Service
public class TaskStateActive implements TaskStateModifier {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskStateActive.class);
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    private final TasksRepository tasksRepository;

    /**
     * Default Constructor. Loads the dependencies.
     *
     * @param taskManager handles the Agent-state changes on Task state change.
     * @param agentsPool  pool of all agents
     */
    @Autowired
    public TaskStateActive(TaskManager taskManager, AgentsPool agentsPool,
                           TasksRepository tasksRepository) {
        this.taskManager = taskManager;
        this.agentsPool = agentsPool;
        this.tasksRepository = tasksRepository;
    }

    @Override
    public void updateState(Task task, TaskState state) {
        Agent agent = this.agentsPool.findById(task.getAssignedTo());
        if (agent == null) {
            logger.error("Could not update task state to Active, Assigned Agent not found");
            return;
        }

        task.setTaskState(state);
        task.setStartTime(System.currentTimeMillis());

        this.tasksRepository.save(task.getId().toString(), AdapterUtility.createTaskDtoFrom(task));

        RoutingMode routingMode = task.getRoutingMode();

        if (routingMode.equals(RoutingMode.PUSH)) {
            this.taskManager.cancelAgentRequestTtlTimerTask(task.getTopicId());
            this.taskManager.removeAgentRequestTtlTimerTask(task.getTopicId());
            agent.assignPushTask(task);
        } else if (routingMode.equals(RoutingMode.EXTERNAL)) {
            agent.assignExternalTask(task);
        }

        this.taskManager.updateAgentMrdState(agent, task.getMrd().getId());
    }
}
