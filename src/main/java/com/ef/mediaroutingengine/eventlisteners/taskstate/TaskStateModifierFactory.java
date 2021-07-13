package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskStateModifierFactory {
    private final TasksRepository tasksRepository;
    private final TasksPool tasksPool;
    private final PrecisionQueuesPool precisionQueuesPool;
    private final AgentsPool agentsPool;
    private final TaskManager taskManager;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param tasksRepository     Tasks Repository DAO
     * @param tasksPool           pool of all tasks
     * @param precisionQueuesPool pool of all precision queues
     * @param taskManager         Manages the Agent/Agent-MRD state changes on task state changes.
     */
    @Autowired
    public TaskStateModifierFactory(TasksRepository tasksRepository, TasksPool tasksPool,
                                    PrecisionQueuesPool precisionQueuesPool, AgentsPool agentsPool,
                                    TaskManager taskManager) {
        this.tasksRepository = tasksRepository;
        this.tasksPool = tasksPool;
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
            return new TaskStateClose(tasksRepository, precisionQueuesPool, tasksPool, taskManager);
        } else if (state.equals(Enums.TaskStateName.ACTIVE)) {
            return new TaskStateActive(taskManager, agentsPool, tasksPool);
        } else {
            return new TaskStateOther();
        }
    }
}
