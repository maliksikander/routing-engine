package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskStateActive implements TaskStateModifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStateActive.class);
    private final TaskManager taskManager;
    private final AgentsPool agentsPool;
    private final TasksPool tasksPool;

    /**
     * Default Constructor. Loads the dependencies.
     *
     * @param taskManager handles the Agent-state changes on Task state change.
     * @param agentsPool pool of all agents
     * @param tasksPool pool of all tasks
     */
    @Autowired
    public TaskStateActive(TaskManager taskManager, AgentsPool agentsPool, TasksPool tasksPool) {
        this.taskManager = taskManager;
        this.agentsPool = agentsPool;
        this.tasksPool = tasksPool;
    }

    @Override
    public void updateState(Task task, TaskState state) {
        Agent agent = this.agentsPool.findById(task.getAssignedTo());
        if (agent == null) {
            LOGGER.error("Could not update task state to Active, Assigned Agent not found");
            return;
        }
        task.setTaskState(state);
        task.setStartTime(System.currentTimeMillis());
        this.tasksPool.cancelAgentRequestTtlTimerTask(task.getTopicId());
        this.tasksPool.removeAgentRequestTtlTimerTask(task.getTopicId());
        agent.assignTask(task);
        this.taskManager.updateAgentMrdState(agent, task.getMrd().getId());
    }
}
