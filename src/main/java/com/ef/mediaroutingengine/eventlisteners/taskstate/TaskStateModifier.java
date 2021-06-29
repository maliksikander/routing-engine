package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;

public interface TaskStateModifier {
    void updateState(Task task, TaskState state);
}
