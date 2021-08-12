package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Tasks service.
 */
@Service
public class TasksService {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    /**
     * The Task retriever factory.
     */
    private final TasksRetrieverFactory tasksRetrieverFactory;

    /**
     * Instantiates a new Tasks service.
     *
     * @param tasksPool             the tasks pool
     * @param tasksRetrieverFactory the task retriever factory
     */
    @Autowired
    public TasksService(TasksPool tasksPool, TasksRetrieverFactory tasksRetrieverFactory) {
        this.tasksPool = tasksPool;
        this.tasksRetrieverFactory = tasksRetrieverFactory;
    }

    /**
     * Retrieve by id task dto.
     *
     * @param taskId the task id
     * @return the task dto
     */
    public TaskDto retrieveById(UUID taskId) {
        Task task = this.tasksPool.findById(taskId);
        if (task != null) {
            return new TaskDto(task);
        } else {
            throw new NotFoundException("Task not found in Task pool");
        }
    }

    /**
     * Retrieve list.
     *
     * @param agentId   the agent id
     * @param taskState the task state
     * @return the list
     */
    public List<TaskDto> retrieve(Optional<UUID> agentId, Optional<Enums.TaskStateName> taskState) {
        TasksRetriever tasksRetriever = this.tasksRetrieverFactory.getRetriever(agentId, taskState);
        return tasksRetriever.findTasks();
    }
}
