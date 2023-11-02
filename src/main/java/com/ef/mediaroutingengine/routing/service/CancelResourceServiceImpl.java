package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.task.Task;
import com.ef.mediaroutingengine.routing.dto.CancelResourceRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type End task service.
 */
@Service
public class CancelResourceServiceImpl implements CancelResourceService {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CancelResourceServiceImpl.class);
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;

    /**
     * Instantiates a new End task service.
     *
     * @param taskManager     the task manager
     * @param tasksRepository the tasks repository
     */
    @Autowired
    public CancelResourceServiceImpl(TaskManager taskManager, TasksRepository tasksRepository) {
        this.taskManager = taskManager;
        this.tasksRepository = tasksRepository;
    }

    @Override
    public void cancelResource(CancelResourceRequest request) {
        logger.info("Cancel resource request initiated for conversation: {}", request.getTopicId());
        List<Task> tasks = this.tasksRepository.findAllByConversation(request.getTopicId());
        tasks.forEach(t -> this.taskManager.revokeInProcessTask(t, true));
    }
}
