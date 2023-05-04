package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.taskmanager.model.Task;

/**
 * The interface Task state modifier.
 */
public interface TaskStateModifier {
    /**
     * Update state.
     *
     * @param task  the task
     * @param state the state
     */
    boolean updateState(Task task, TaskState state);
}
