package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import com.ef.cim.objectmodel.dto.TaskDto;
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