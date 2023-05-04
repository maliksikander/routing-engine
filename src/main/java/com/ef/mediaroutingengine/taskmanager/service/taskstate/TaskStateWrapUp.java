package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;

/**
 * Task type Wrap Up Implementation.
 */
public class TaskStateWrapUp implements TaskStateModifier {
    /**
     * The JMS Communicator.
     */
    private final JmsCommunicator jmsCommunicator;
    /**
     * Repository to store tasks.
     */
    private final TasksRepository tasksRepository;

    public TaskStateWrapUp(TasksRepository tasksRepository, JmsCommunicator jmsCommunicator) {
        this.tasksRepository = tasksRepository;
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public boolean updateState(Task task, TaskState state) {
        if (!task.getTaskState().getName().equals(Enums.TaskStateName.ACTIVE)) {
            return false;
        }

        task.setTaskState(state);
        tasksRepository.save(task.getId(), AdapterUtility.createTaskDtoFrom(task));
        jmsCommunicator.publishTaskStateChangeForReporting(task);

        return true;
    }
}
