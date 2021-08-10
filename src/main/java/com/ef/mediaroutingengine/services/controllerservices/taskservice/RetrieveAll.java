package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.TasksPool;
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
            result.add(new TaskDto(task));
        }
        return result;
    }
}
