package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Retrieve all.
 */
public class RetrieveAll implements TasksRetriever {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    /**
     * Instantiates a new Retrieve all.
     *
     * @param tasksPool the tasks pool
     */
    public RetrieveAll(TasksPool tasksPool) {
        this.tasksPool = tasksPool;
    }

    @Override
    public List<TaskDto> findTasks() {
        List<TaskDto> result = new ArrayList<>();
        for (Task task : this.tasksPool.findAll()) {
            result.add(AdapterUtility.createTaskDtoFrom(task));
        }
        return result;
    }
}
