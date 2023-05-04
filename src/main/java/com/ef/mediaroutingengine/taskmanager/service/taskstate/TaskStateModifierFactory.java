package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Task state modifier factory.
 */
@Service
public class TaskStateModifierFactory {
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param tasksRepository     Tasks Repository DAO
     * @param precisionQueuesPool pool of all precision queues
     * @param agentsPool          the agents pool
     * @param taskManager         Manages the Agent/Agent-MRD state changes on task state changes.
     */
    @Autowired
    public TaskStateModifierFactory(TasksRepository tasksRepository, PrecisionQueuesPool precisionQueuesPool,
                                    AgentsPool agentsPool, TasksPool tasksPool,
                                    TaskManager taskManager, JmsCommunicator jmsCommunicator) {
        this.tasksRepository = tasksRepository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentsPool = agentsPool;
        this.tasksPool = tasksPool;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Returns the Task state modifier object for the requested state change.
     *
     * @param state the requested Task state to be changed
     * @return Task State Modifier for the requested state change
     */
    public TaskStateModifier getModifier(Enums.TaskStateName state) {
        if (state.equals(Enums.TaskStateName.CLOSED)) {
            return new TaskStateClose(precisionQueuesPool, tasksPool, taskManager, jmsCommunicator);
        } else if (state.equals(Enums.TaskStateName.ACTIVE)) {
            return new TaskStateActive(taskManager, agentsPool, tasksRepository, jmsCommunicator);
        } else if (state.equals(Enums.TaskStateName.WRAP_UP)) {
            return new TaskStateWrapUp(tasksRepository, jmsCommunicator);
        } else {
            return new TaskStateOther();
        }
    }
}
