package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskState;

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
