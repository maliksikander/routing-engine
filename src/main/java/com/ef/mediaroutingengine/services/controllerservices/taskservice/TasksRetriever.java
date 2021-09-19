package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import com.ef.mediaroutingengine.dto.TaskDto;
import java.util.List;

/**
 * The interface Tasks retriever.
 */
public interface TasksRetriever {
    /**
     * Find tasks list.
     *
     * @return the list
     */
    List<TaskDto> findTasks();
}