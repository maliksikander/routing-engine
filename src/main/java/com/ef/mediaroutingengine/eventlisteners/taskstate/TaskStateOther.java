package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.model.Task;

/**
 * The type Task state other.
 */
public class TaskStateOther implements TaskStateModifier {

    @Override
    public void updateState(Task task, TaskState state) {
        task.setTaskState(state);
    }
}
