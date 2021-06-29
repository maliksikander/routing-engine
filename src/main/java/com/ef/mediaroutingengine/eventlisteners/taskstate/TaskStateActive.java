package com.ef.mediaroutingengine.eventlisteners.taskstate;

import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskStateActive implements TaskStateModifier {
    private final TaskManager taskManager;

    @Autowired
    public TaskStateActive(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void updateState(Task task, TaskState state) {
        task.setTaskState(state);
        task.setStartTime(System.currentTimeMillis());
        this.taskManager.updateAgentMrdState(task);
    }
}
