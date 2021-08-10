package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;

/**
 * The type Task state other.
 */
public class TaskStateOther implements TaskStateModifier {

    @Override
    public void updateState(Task task, TaskState state) {
        task.setTaskState(state);
    }
}