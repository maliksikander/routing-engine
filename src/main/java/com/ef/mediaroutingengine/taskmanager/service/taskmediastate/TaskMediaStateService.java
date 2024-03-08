package com.ef.mediaroutingengine.taskmanager.service.taskmediastate;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
import com.ef.mediaroutingengine.global.locks.ConversationLock;
import com.ef.mediaroutingengine.routing.AgentRequestTimerService;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.dto.MediaStateChangeReq;
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

    private final AgentRequestTimerService agentRequestTimerService;
    private final AgentsPool agentsPool;
    private final JmsCommunicator jmsCommunicator;
    private final ConversationLock conversationLock = new ConversationLock();

    /**
     * Instantiates a new Task media state service.
     *
     * @param tasksRepository          the tasks repository
     * @param taskManager              the task manager
     * @param agentRequestTimerService the agent request timer service
     * @param agentsPool               the agents pool
     * @param jmsCommunicator          the jms communicator
     */
    @Autowired
    public TaskMediaStateService(TasksRepository tasksRepository, TaskManager taskManager,
                                 AgentRequestTimerService agentRequestTimerService, AgentsPool agentsPool,
                                 JmsCommunicator jmsCommunicator) {
        this.tasksRepository = tasksRepository;
        this.taskManager = taskManager;
        this.agentRequestTimerService = agentRequestTimerService;
        this.agentsPool = agentsPool;
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Handle.
     *
     * @param taskId  the task id
     * @param mediaId the media id
     * @param request the request
     * @return the task dto
     */
    public Task changeState(String taskId, String mediaId, MediaStateChangeReq request) {
        TaskMediaState state = request.getState();

        try {
            conversationLock.lock(request.getConversationId());

            Task task = this.tasksRepository.find(taskId);

            if (task == null) {
                logger.error("Task not found for id: {}", taskId);
                return null;
            }

            TaskMedia taskMedia = task.findMediaBy(mediaId);

            if (taskMedia == null) {
                logger.error("Task Media not found for id: {}", mediaId);
                return task;
            }

            switch (state) {
                case ACTIVE -> handleActive(task, taskMedia);
                case CLOSED -> handleClosed(task, taskMedia);
                default -> logger.info("{} state not allowed in the task media state change API", state);
            }

            return task;
        } finally {
            conversationLock.unlock(request.getConversationId());
        }
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

    private void handleClosed(@NotNull Task task, @NotNull TaskMedia taskMedia) {

        if (task.getState().getName().equals(Enums.TaskStateName.ACTIVE)
                && taskMedia.getState().equals(TaskMediaState.RESERVED)) {

            this.agentRequestTimerService.stop(task.getAgentRequestTtlTimerId());
            this.agentsPool.findBy(task.getAssignedTo()).removeReservedTask();

            taskMedia.setState(TaskMediaState.CLOSED);

            ChannelSession session = taskMedia.getRequestSession();

            if (task.isRemovable()) {
                this.tasksRepository.delete(task);
                task.setState(new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.CANCELLED));
                jmsCommunicator.publishTaskStateChanged(task, session, true, taskMedia.getId());
            } else {
                jmsCommunicator.publishTaskStateChanged(task, session, false, taskMedia.getId());
                task.removeMedia(taskMedia.getId());
                tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());
            }

            return;
        }

        logger.error("Media State change from {} to CLOSED when Task state is {} is not implemented yet",
                taskMedia.getState(), task.getState());
    }
}
