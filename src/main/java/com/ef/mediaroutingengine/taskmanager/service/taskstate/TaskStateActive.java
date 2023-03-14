package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
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

/**
 * The type Task state active.
 */
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
    public TaskStateActive(TaskManager taskManager, AgentsPool agentsPool,
                           TasksRepository tasksRepository, JmsCommunicator jmsCommunicator) {
        this.taskManager = taskManager;
        this.agentsPool = agentsPool;
        this.tasksRepository = tasksRepository;
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public boolean updateState(Task task, TaskState state) {
        Agent agent = this.agentsPool.findById(task.getAssignedTo());

        if (agent == null) {
            logger.error("Could not update task state to Active, Assigned Agent not found");
            return false;
        }

        TaskState currentState = task.getTaskState();

        task.setTaskState(state);

        if (!currentState.getName().equals(Enums.TaskStateName.WRAP_UP)) {
            task.setStartTime(System.currentTimeMillis());
        }

        this.tasksRepository.save(task.getId(), AdapterUtility.createTaskDtoFrom(task));
        this.jmsCommunicator.publishTaskStateChangeForReporting(task);

        if (!currentState.getName().equals(Enums.TaskStateName.WRAP_UP)) {
            if (task.getType().getMode().equals(Enums.TaskTypeMode.QUEUE)) {
                this.taskManager.cancelAgentRequestTtlTimerTask(task.getTopicId());
                this.taskManager.removeAgentRequestTtlTimerTask(task.getTopicId());

                agent.removeReservedTask();
                agent.addActiveTask(task);

                this.taskManager.updateAgentMrdState(agent, task.getMrd().getId());
            } else {
                agent.addActiveTask(task);
            }
        }

        return true;
    }
}
