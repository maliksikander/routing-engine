package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;

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
