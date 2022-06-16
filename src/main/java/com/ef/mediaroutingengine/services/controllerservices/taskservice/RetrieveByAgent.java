package com.ef.mediaroutingengine.services.controllerservices.taskservice;

import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import com.ef.mediaroutingengine.services.utilities.AdapterUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final UUID agentId;

    /**
     * Instantiates a new Retrieve by agent.
     *
     * @param tasksPool the tasks pool
     * @param agentId   the agent id
     */
    public RetrieveByAgent(TasksPool tasksPool, UUID agentId) {
        this.tasksPool = tasksPool;
        this.agentId = agentId;
    }

    @Override
    public List<TaskDto> findTasks() {
        List<TaskDto> result = new ArrayList<>();
        for (Task task : tasksPool.findAll()) {
            UUID assignedTo = task.getAssignedTo();
            if (assignedTo != null && assignedTo.equals(agentId)) {
                result.add(AdapterUtility.createTaskDtoFrom(task));
            }
        }
        return result;
    }
}
