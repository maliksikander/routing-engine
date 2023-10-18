package com.ef.mediaroutingengine.taskmanager.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.dto.QueueHistoricalStatsDto;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.jms.JmsCommunicator;
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
    private static final Logger logger = LoggerFactory.getLogger(TasksService.class);

    private final PrecisionQueuesPool precisionQueuesCache;
    private final TasksRepository tasksRepository;
    private final MrdPool mrdPool;
    private final RestRequest restRequest;
    private final TaskManager taskManager;
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Tasks service.
     *
     * @param precisionQueuesCache the queues pool
     * @param tasksRepository      the tasks repository
     * @param restRequest          the rest request
     */
    @Autowired
    public TasksService(PrecisionQueuesPool precisionQueuesCache, TasksRepository tasksRepository, MrdPool mrdPool,
                        RestRequest restRequest, TaskManager taskManager, JmsCommunicator jmsCommunicator) {
        this.precisionQueuesCache = precisionQueuesCache;
        this.tasksRepository = tasksRepository;
        this.mrdPool = mrdPool;
        this.restRequest = restRequest;
        this.taskManager = taskManager;
        this.jmsCommunicator = jmsCommunicator;
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
     * Calls the required methods for EWT and position.
     *
     * @param conversationId The conversation id.
     * @return The response entity.
     */
    public ResponseEntity<Object> getEwtAndPosition(String conversationId) {
        logger.info("Request received to fetch the EWT and position for conversation id: {}", conversationId);

        List<TaskEwtResponse> responses = new ArrayList<>();
        Map<String, List<Task>> queuedTasks = this.tasksRepository.findQueuedGroupedByQueueId(conversationId);

        for (Map.Entry<String, List<Task>> entry : queuedTasks.entrySet()) {
            QueueHistoricalStatsDto queueStats = this.restRequest.getQueueHistoricalStats(entry.getKey());

            PrecisionQueue precisionQueue = precisionQueuesCache.findById(entry.getKey());
            int totalAgents = precisionQueue.getAssociatedAgents().size();

            synchronized (precisionQueue.getServiceQueue()) {
                for (Task task : entry.getValue()) {
                    int position = precisionQueue.getPosition(task);

                    if (position == -1) {
                        continue;
                    }

                    int ewt = this.calculateEwt(position, totalAgents, queueStats.getAverageHandleTime());
                    responses.add(new TaskEwtResponse(task, ewt, position));
                }
            }
        }

        logger.info("Request to fetch the EWT and position for conversation id: {} handled", conversationId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    private int calculateEwt(int taskPosition, int totalAgents, int averageHandleTime) {
        return totalAgents == 0 ? Integer.MAX_VALUE : taskPosition * averageHandleTime / totalAgents;
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
        List<Task> tasks = this.tasksRepository.findAllByConversation(conversationId).stream()
                .filter(t -> t.getState().getName().equals(Enums.TaskStateName.ACTIVE))
                .toList();

        for (Task task : tasks) {
            TaskMedia media = task.findMediaByMrdId(mrdId);

            if (media != null) {
                media.addChannelSession(channelSession);
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
        List<Task> tasks = this.tasksRepository.findAllByConversation(conversationId).stream()
                .filter(t -> t.getState().getName().equals(Enums.TaskStateName.ACTIVE))
                .toList();

        for (Task task : tasks) {
            TaskMedia media = task.findMediaByMrdId(mrdId);

            if (media == null) {
                continue;
            }

            media.removeChannelSession(channelSession.getId());
            if (media.getChannelSessions().isEmpty() && !media.getState().equals(TaskMediaState.ACTIVE)) {
                this.revokeMedia(task, media);
                task.removeMedia(media.getId());
            }

            if (task.isRemovable()) {
                this.tasksRepository.deleteById(task.getId());
                task.setState(new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.CANCELLED));
                this.jmsCommunicator.publishTaskStateChanged(task, media.getRequestSession());
            }
        }
    }

    /**
     * Close media.
     *
     * @param task      the task
     * @param taskMedia the task media
     */
    private void revokeMedia(Task task, TaskMedia taskMedia) {
        if (taskMedia.getState().equals(TaskMediaState.QUEUED)) {
            this.taskManager.revokeQueuedMedia(task, taskMedia);
        } else if (taskMedia.getState().equals(TaskMediaState.RESERVED)) {
            this.taskManager.revokeReservedMedia(task, taskMedia);
        }
    }
}