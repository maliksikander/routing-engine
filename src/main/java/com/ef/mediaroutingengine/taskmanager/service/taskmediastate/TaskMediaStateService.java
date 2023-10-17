package com.ef.mediaroutingengine.taskmanager.service.taskmediastate;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Task media state handler.
 */
@Service
public class TaskMediaStateService {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskMediaStateService.class);
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;

    /**
     * Instantiates a new Task media state handler.
     *
     * @param tasksRepository the tasks repository
     * @param taskManager     the task manager
     */
    @Autowired
    public TaskMediaStateService(TasksRepository tasksRepository, TaskManager taskManager) {
        this.tasksRepository = tasksRepository;
        this.taskManager = taskManager;
    }

    /**
     * Handle.
     *
     * @param taskId  the task id
     * @param mediaId the media id
     * @param state   the state
     * @return the task dto
     */
    public Task changeState(String taskId, String mediaId, TaskMediaState state) {
        Task task = this.tasksRepository.find(taskId);

        if (!state.equals(TaskMediaState.ACTIVE)) {
            logger.info("{} state not allowed in the task media state change API", state);
            return task;
        }

        if (task == null) {
            logger.error("Task not found for id: {}", taskId);
            return null;
        }

        TaskMedia taskMedia = task.findMediaBy(mediaId);

        if (taskMedia == null) {
            logger.error("Task Media not found for id: {}", mediaId);
            return task;
        }

        this.handleActive(task, taskMedia);
        return task;
    }

    /**
     * Handle active.
     *
     * @param task      the task
     * @param taskMedia the task media
     */
    private void handleActive(@NotNull Task task, @NotNull TaskMedia taskMedia) {
        if (!task.getState().getName().equals(Enums.TaskStateName.ACTIVE)
                && !(taskMedia.getState().equals(TaskMediaState.RESERVED)
                || taskMedia.getState().equals(TaskMediaState.STARTED))) {
            logger.error("Can't change Media State to Active, Current task state: {}, media state: {}",
                    task.getState(), taskMedia.getState());
            return;
        }

        this.taskManager.activateMedia(task, taskMedia);
    }
}
