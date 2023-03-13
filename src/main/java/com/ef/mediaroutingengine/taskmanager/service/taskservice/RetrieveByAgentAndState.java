package com.ef.mediaroutingengine.taskmanager.service.taskservice;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Retrieve by agent and state.
 */
public class RetrieveByAgentAndState implements TasksRetriever {
    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;
    /**
     * The Agent id.
     */
    private final String agentId;
    /**
     * The State name.
     */
    private final Enums.TaskStateName stateName;

    /**
     * Instantiates a new Retrieve by agent and state.
     *
     * @param tasksPool the tasks pool
     * @param agentId   the agent id
     * @param stateName the state name
     */
    public RetrieveByAgentAndState(TasksPool tasksPool, String agentId, Enums.TaskStateName stateName) {
        this.tasksPool = tasksPool;
        this.agentId = agentId;
        this.stateName = stateName;
    }

    @Override
    public List<TaskDto> findTasks() {
        List<TaskDto> result = new ArrayList<>();
        for (Task task : tasksPool.findAll()) {
            String assignedTo = task.getAssignedTo();
            if (assignedTo != null && assignedTo.equals(this.agentId)
                    && task.getTaskState().getName().equals(this.stateName)) {
                result.add(AdapterUtility.createTaskDtoFrom(task));
            }
        }
        return result;
    }
}
