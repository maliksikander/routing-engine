package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.taskmanager.model.Task;

/**
 * The type Task state other.
 */
public class TaskStateOther implements TaskStateModifier {
    @Override
    public boolean updateState(Task task, TaskState state) {
        task.setTaskState(state);
        return true;
    }
}
