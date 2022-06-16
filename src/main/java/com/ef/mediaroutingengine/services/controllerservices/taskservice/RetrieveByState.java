package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.utilities.AdapterUtility;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Retrieve by state.
 */
public class RetrieveByState implements TasksRetriever {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The State name.
     */
    private final Enums.TaskStateName stateName;

    /**
     * Instantiates a new Retrieve by state.
     *
     * @param tasksPool the tasks pool
     * @param stateName the state name
     */
    public RetrieveByState(TasksPool tasksPool, Enums.TaskStateName stateName) {
        this.tasksPool = tasksPool;
        this.stateName = stateName;
    }

    @Override
    public List<TaskDto> findTasks() {
        List<TaskDto> result = new ArrayList<>();
        for (Task task : this.tasksPool.findAll()) {
            if (task.getTaskState().getName().equals(this.stateName)) {
                result.add(AdapterUtility.createTaskDtoFrom(task));
            }
        }
        return result;
    }
}
