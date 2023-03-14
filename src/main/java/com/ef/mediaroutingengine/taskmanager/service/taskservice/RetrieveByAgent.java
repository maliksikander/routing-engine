package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import com.ef.cim.objectmodel.TaskAgent;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Retrieve by agent.
 */
public class RetrieveByAgent implements TasksRetriever {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Agent id.
     */
    private final String agentId;

    /**
     * Instantiates a new Retrieve by agent.
     *
     * @param tasksPool the tasks pool
     * @param agentId   the agent id
     */
    public RetrieveByAgent(TasksPool tasksPool, String agentId) {
        this.tasksPool = tasksPool;
        this.agentId = agentId;
    }

    @Override
    public List<TaskDto> findTasks() {
        List<TaskDto> result = new ArrayList<>();
        for (Task task : tasksPool.findAll()) {
            TaskAgent assignedTo = task.getAssignedTo();
            if (assignedTo != null && assignedTo.getId().equals(agentId)) {
                result.add(AdapterUtility.createTaskDtoFrom(task));
            }
        }
        return result;
    }
}
