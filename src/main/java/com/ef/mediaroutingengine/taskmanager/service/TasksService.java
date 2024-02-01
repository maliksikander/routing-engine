package com.ef.mediaroutingengine.taskmanager.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.QueueHistoricalStatsDto;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.locks.ConversationLock;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.dto.TaskEwtResponse;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * The type Tasks service.
 */
@Service
public class TasksService {
    /**
     * The constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(TasksService.class);

    /**
     * The Precision queues cache.
     */
    private final PrecisionQueuesPool precisionQueuesCache;
    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;
    /**
     * The Rest request.
     */
    private final RestRequest restRequest;
    /**
     * The Task manager.
     */
    private final TaskManager taskManager;
    private final ConversationLock conversationLock = new ConversationLock();

    /**
     * Instantiates a new Tasks service.
     *
     * @param precisionQueuesCache the queues pool
     * @param tasksRepository      the tasks repository
     * @param mrdPool              the mrd pool
     * @param restRequest          the rest request
     * @param taskManager          the task manager
     */
    @Autowired
    public TasksService(PrecisionQueuesPool precisionQueuesCache, TasksRepository tasksRepository, MrdPool mrdPool,
                        RestRequest restRequest, TaskManager taskManager) {
        this.precisionQueuesCache = precisionQueuesCache;
        this.tasksRepository = tasksRepository;
        this.mrdPool = mrdPool;
        this.restRequest = restRequest;
        this.taskManager = taskManager;
    }

    /**
     * Retrieve by id task dto.
     *
     * @param taskId the task id
     * @return the task dto
     */
    public Task retrieveById(String taskId) {
        Task task = this.tasksRepository.find(taskId);

        if (task == null) {
            throw new NotFoundException("Task not found in Task pool");
        }

        return task;
    }

    /**
     * Retrieve list.
     *
     * @param agentId the agent id
     * @return the list
     */
    public List<Task> retrieveAll(Optional<String> agentId) {
        if (agentId.isPresent()) {
            return this.tasksRepository.findAllByAgent(agentId.get());
        }
        return this.tasksRepository.findAll();
    }

    /**
     * Cancel resource by direction.
     *
     * @param conversationId the conversation id
     * @param direction      the direction
     */
    public void revokeResourceByDirection(String conversationId, Enums.TaskTypeDirection direction) {
        if (!direction.equals(Enums.TaskTypeDirection.DIRECT_CONFERENCE)) {
            logger.error("Direction: {} is not supported in this API yet", direction);
            return;
        }

        try {
            conversationLock.lock(conversationId);

            List<Task> tasks = this.tasksRepository.findAllByConversationId(conversationId).stream()
                    .filter(t -> {
                        TaskMedia media = t.findInProcessMedia();
                        return media != null && media.getType().getDirection().equals(direction);
                    }).toList();

            tasks.forEach(t -> this.taskManager.revokeInProcessTask(t, true));
        } finally {
            conversationLock.unlock(conversationId);
        }
    }

    /**
     * Calls the required methods for EWT and position.
     *
     * @param conversationId The conversation id.
     * @return The response entity.
     */
    public ResponseEntity<Object> getEwtAndPosition(String conversationId) {
        logger.info("Request received to fetch the EWT and position for conversation id: {}", conversationId);

        try {
            conversationLock.lock(conversationId);

            List<TaskEwtResponse> responses = new ArrayList<>();
            Map<String, List<Task>> queuedTasks = this.tasksRepository.findQueuedGroupedByQueueId(conversationId);

            for (Map.Entry<String, List<Task>> entry : queuedTasks.entrySet()) {
                QueueHistoricalStatsDto queueStats = this.restRequest.getQueueHistoricalStats(entry.getKey());

                PrecisionQueue precisionQueue = precisionQueuesCache.findById(entry.getKey());
                int totalAgents = precisionQueue.getAssociatedAgents().size();

                for (Task task : entry.getValue()) {
                    int position = precisionQueue.getPosition(task);

                    if (position == -1) {
                        continue;
                    }

                    int ewt = this.calculateEwt(position, totalAgents, queueStats.getAverageHandleTime(),
                            precisionQueue.getEwtMinValue(), precisionQueue.getEwtMaxValue());
                    logger.info("Calculated ewt is {}", ewt);
                    responses.add(new TaskEwtResponse(task, ewt, position));
                }
            }


            logger.info("Request to fetch the EWT and position for conversation id: {} handled", conversationId);
            return new ResponseEntity<>(responses, HttpStatus.OK);

        } finally {
            conversationLock.unlock(conversationId);
        }
    }

    /**
     * Calculate ewt value.
     *
     * @param taskPosition      the task position
     * @param totalAgents       the total agents
     * @param averageHandleTime the average handle time
     * @param ewtMinValue       the min ewt value cap.
     * @param ewtMaxValue       the max ewt value cap.
     * @return the ewt value.
     */
    private int calculateEwt(int taskPosition, int totalAgents, int averageHandleTime, Integer ewtMinValue,
                             Integer  ewtMaxValue) {
        int ewt = totalAgents == 0 ? ewtMaxValue : taskPosition * averageHandleTime / totalAgents;

        // Ensure that ewt is within the specified range
        if (ewtMinValue != null && ewt < ewtMinValue) {
            logger.info("EWT value is {} which is less than the min bound that is {}, setting it to {}", ewt,
                    ewtMinValue, ewtMinValue);
            ewt = ewtMinValue;
        } else if (ewtMaxValue != null && ewt > ewtMaxValue) {
            logger.info("EWT value is {} which is higher than the max bound that is {}, setting it to {}", ewt,
                    ewtMaxValue, ewtMaxValue);
            ewt = ewtMaxValue;
        }
        return ewt;
    }

    /**
     * Add session.
     *
     * @param channelSession the channel session
     */
    public void addSession(ChannelSession channelSession) {
        String mrdId = channelSession.getChannel().getChannelType().getMediaRoutingDomain();

        if (!this.mrdPool.getType(mrdId).isAutoJoin()) {
            return;
        }

        String conversationId = channelSession.getConversationId();
        List<Task> tasks = this.tasksRepository.findAllByConversationId(conversationId).stream()
                .filter(t -> t.getState().getName().equals(Enums.TaskStateName.ACTIVE))
                .toList();

        for (Task task : tasks) {
            TaskMedia media = task.findMediaByMrdId(mrdId);

            if (media != null) {
                media.addChannelSession(channelSession);
                this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());
            }
        }
    }

    /**
     * Remove session.
     *
     * @param channelSession the channel session
     */
    public void removeSession(ChannelSession channelSession) {
        String mrdId = channelSession.getChannel().getChannelType().getMediaRoutingDomain();
        String conversationId = channelSession.getConversationId();

        try {
            conversationLock.lock(conversationId);

            List<Task> tasks = this.tasksRepository.findAllByConversationId(conversationId).stream()
                    .filter(t -> t.getState().getName().equals(Enums.TaskStateName.ACTIVE))
                    .toList();

            for (Task task : tasks) {
                TaskMedia media = task.findMediaByMrdId(mrdId);

                if (media == null) {
                    continue;
                }

                boolean isRemoved = media.removeChannelSession(channelSession.getId());

                if (isRemoved) {
                    this.tasksRepository.updateActiveMedias(task.getId(), task.getActiveMedia());

                    if (media.getChannelSessions().isEmpty() && !media.getState().equals(TaskMediaState.ACTIVE)) {
                        this.taskManager.revokeInProcessTask(task, false);
                    }
                }
            }
        } finally {
            conversationLock.unlock(conversationId);
        }
    }
}