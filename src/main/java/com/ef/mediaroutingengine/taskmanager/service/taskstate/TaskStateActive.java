package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
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
    /**
     * The Tasks Repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Default Constructor. Loads the dependencies.
     *
     * @param taskManager handles the Agent-state changes on Task state change.
     * @param agentsPool  pool of all agents
     */
    @Autowired
    public TaskStateActive(TaskManager taskManager, AgentsPool agentsPool,
                           TasksRepository tasksRepository, JmsCommunicator jmsCommunicator) {
        this.taskManager = taskManager;
        this.agentsPool = agentsPool;
        this.tasksRepository = tasksRepository;
        this.jmsCommunicator = jmsCommunicator;
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

        this.tasksRepository.save(task.getId(), AdapterUtility.createTaskDtoFrom(task));
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        if (task.getRoutingMode().equals(RoutingMode.PUSH)) {
            this.taskManager.cancelAgentRequestTtlTimerTask(task.getTopicId());
            this.taskManager.removeAgentRequestTtlTimerTask(task.getTopicId());
            agent.assignPushTask(task);

            this.taskManager.updateAgentMrdState(agent, task.getMrd().getId());
        } else {
            agent.addActiveTask(task);
        }
    }
}
