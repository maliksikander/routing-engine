package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;

public class TaskStateClose implements TaskStateModifier {
    private final TasksRepository tasksRepository;
    private final PrecisionQueuesPool precisionQueuesPool;
    private final TasksPool tasksPool;
    private final TaskManager taskManager;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param tasksRepository     Tasks Repository DAO
     * @param precisionQueuesPool Pool of all precision queues
     * @param tasksPool           pool of all tasks
     * @param taskManager         handles tasks closing.
     */
    public TaskStateClose(TasksRepository tasksRepository, PrecisionQueuesPool precisionQueuesPool,
                          TasksPool tasksPool, TaskManager taskManager) {
        this.tasksRepository = tasksRepository;
        this.tasksPool = tasksPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.taskManager = taskManager;
    }


    @Override
    public void updateState(Task task, TaskState state) {
        this.precisionQueuesPool.endTask(task);

        if (state.getReasonCode() == null) {
            this.taskManager.endTaskFromAssignedAgent(task);
            this.tasksRepository.deleteById(task.getId().toString());
            this.tasksPool.removeTask(task);
        } else if (state.getReasonCode().equals(Enums.TaskStateReasonCode.RONA)) {
            this.taskManager.endTaskFromAgentOnRona(task);
            this.tasksPool.rerouteTask(task);
        }
    }
}
