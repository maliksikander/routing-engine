package com.ef.mediaroutingengine.taskmanager.service.taskstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Task type Wrap Up Implementation.
 */
public class TaskStateWrapUp implements TaskStateModifier {
    /**
     * The Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(TaskStateWrapUp.class);
    /**
     * The JMS Communicator.
     */
    private JmsCommunicator jmsCommunicator;
    /**
     * Repository to store tasks.
     */
    private TasksRepository tasksRepository;

    @Autowired
    public TaskStateWrapUp(TasksRepository tasksRepository, JmsCommunicator jmsCommunicator) {
        this.tasksRepository = tasksRepository;
        this.jmsCommunicator = jmsCommunicator;
    }

    @Override
    public void updateState(Task task, TaskState state) {
        if (!task.getTaskState().getName().equals(Enums.TaskStateName.ACTIVE)) {
            String error = "Task is not active, could not change the task state to wrap up.";
            logger.error(error);
            throw new IllegalStateException(error);
        }

        task.setTaskState(state);

        tasksRepository.save(task.getId(), AdapterUtility.createTaskDtoFrom(task));
        jmsCommunicator.publishTaskStateChangeForReporting(task);

    }
}
