package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
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
     * The Task manager.
     */
    private final TaskManager taskManager;

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
                                    AgentsPool agentsPool, TaskManager taskManager) {
        this.tasksRepository = tasksRepository;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentsPool = agentsPool;
        this.taskManager = taskManager;
    }

    /**
     * Returns the Task state modifier object for the requested state change.
     *
     * @param state the requested Task state to be changed
     * @return Task State Modifier for the requested state change
     */
    public TaskStateModifier getModifier(Enums.TaskStateName state) {
        if (state.equals(Enums.TaskStateName.CLOSED)) {
            return new TaskStateClose(tasksRepository, precisionQueuesPool, taskManager);
        } else if (state.equals(Enums.TaskStateName.ACTIVE)) {
            return new TaskStateActive(taskManager, agentsPool);
        } else {
            return new TaskStateOther();
        }
    }
}
