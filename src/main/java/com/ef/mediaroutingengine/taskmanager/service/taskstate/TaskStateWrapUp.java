package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
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
        if (!task.getState().getName().equals(Enums.TaskStateName.ACTIVE)) {
            return false;
        }

        task.setState(state);
        tasksRepository.save(task.getId(), task);
        //jmsCommunicator.publishTaskStateChangeForReporting(task);

        return true;
    }
}
