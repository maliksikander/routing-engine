package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Task;

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
    void updateState(Task task, TaskState state);
}
