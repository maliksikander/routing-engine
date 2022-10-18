package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.TaskType;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.routing.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.taskmanager.TaskManager;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Assign resource service.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignResourceServiceImpl implements AssignResourceService {

    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(AssignResourceServiceImpl.class);

    /**
     * The Task manager.
     */
    private final TaskManager taskManager;

    private final TasksPool tasksPool;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param taskManager         the task manager
     * @param precisionQueuesPool the precision queues pool
     */
    @Autowired
    public AssignResourceServiceImpl(TaskManager taskManager, TasksPool tasksPool,
                                     PrecisionQueuesPool precisionQueuesPool) {
        this.taskManager = taskManager;
        this.tasksPool = tasksPool;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    @Override
    public String assign(AssignResourceRequest request, boolean useQueueName) {
        String conversationId = request.getChannelSession().getConversationId();
        logger.info("Assign resource request initiated | Conversation: {}", conversationId);

        this.throwExceptionIfRequestExistsFor(conversationId);

        if (request.getRequestType() == null) {
            TaskType type = new TaskType(Enums.TaskTypeDirection.INBOUND, Enums.TaskTypeMode.QUEUE, null);
            request.setRequestType(type);
        }
        validateRequestTypeMode(request.getRequestType());

        ChannelSession channelSession = request.getChannelSession();
        validateChannelSession(channelSession);
        logger.debug("ChannelSession validated in Assign-Resource API request");

        PrecisionQueue queue = this.validateAndGetQueue(channelSession, request.getQueue(), useQueueName);
        logger.debug("PrecisionQueue validated in Assign-Resource API request");

        MediaRoutingDomain mrd = queue.getMrd();

        // TODO: Executor service .. don't use completableFuture!
        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
        CompletableFuture.runAsync(() -> {
            // putting same correlation id and topic id from the caller thread into this thread
            MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
            MDC.put(Constants.MDC_TOPIC_ID, channelSession.getConversationId());
            this.taskManager.enqueueTask(channelSession, queue, mrd, request.getRequestType());
            MDC.clear();
        });

        logger.info("Assign resource request handled gracefully | Topic: {}",
                request.getChannelSession().getConversationId());
        return "The request is received Successfully";
    }

    void throwExceptionIfRequestExistsFor(String conversationId) {
        List<Task> existingTasks = this.tasksPool.findByConversationId(conversationId);

        for (Task task : existingTasks) {
            Enums.TaskStateName stateName = task.getTaskState().getName();

            if (stateName.equals(Enums.TaskStateName.QUEUED) || stateName.equals(Enums.TaskStateName.RESERVED)) {
                String error = "A previous request is still in process of finding agent on this conversation";
                logger.error(error);
                throw new IllegalStateException(error);
            }
        }
    }

    /**
     * Validates Mode of request.
     *
     * @param type Type of the Task
     */
    public void validateRequestTypeMode(TaskType type) {
        if (type.getMode() == null || type.getMode() != Enums.TaskTypeMode.QUEUE) {
            throw new IllegalArgumentException("Invalid request mode, it should be QUEUE");
        }
    }

    /**
     * Validate channel session.
     *
     * @param channelSession the channel session
     */
    void validateChannelSession(ChannelSession channelSession) {
        if (channelSession == null) {
            throw new IllegalArgumentException("Channel Session is null");
        }
        if (channelSession.getChannel() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel is null");
        }
        if (channelSession.getChannel().getChannelConnector() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConnector is null");
        }
        if (channelSession.getChannel().getChannelType() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConnector.ChannelType is null");
        }
        if (channelSession.getChannel().getChannelConfig() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConfig is null");
        }
        if (channelSession.getChannel().getChannelConfig().getRoutingPolicy() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConfig.RoutingPolicy is null");
        }
        if (!channelSession.getChannel().getChannelConfig().getRoutingPolicy()
                .getRoutingMode().equals(RoutingMode.PUSH)) {
            throw new IllegalArgumentException("Routing mode must be PUSH for this request");
        }
    }

    /**
     * Validate and get queue precision queue.
     *
     * @param channelSession the channel session
     * @param requestQueue   the request queue
     * @return the precision queue
     */
    PrecisionQueue validateAndGetQueue(ChannelSession channelSession, String requestQueue,
                                       boolean useQueueName) {
        String defaultQueue = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getRoutingObjectId();
        if (defaultQueue == null && requestQueue == null) {
            throw new IllegalArgumentException("DefaultQueue and RequestedQueue both are null");
        }
        PrecisionQueue queue = getPrecisionQueueFrom(requestQueue, defaultQueue, useQueueName);
        if (queue == null) {
            throw new IllegalArgumentException("Could not find PrecisionQueue for this request");
        }
        if (queue.getSteps().isEmpty()) {
            throw new IllegalStateException("Cannot process request, Queue: " + queue.getId()
                    + " has no steps configured");
        }
        return queue;
    }

    /**
     * Gets precision queue from.
     *
     * @param requestedQueue the requested queue
     * @param defaultQueue   the default queue
     * @return the precision queue from
     */
    PrecisionQueue getPrecisionQueueFrom(String requestedQueue, String defaultQueue, boolean useQueueName) {
        PrecisionQueue queue;

        if (useQueueName) {
            queue = this.precisionQueuesPool.findByName(requestedQueue);
        } else {
            queue = this.precisionQueuesPool.findById(requestedQueue);
        }

        // If requested queue not found, use default queue
        if (queue == null) {
            queue = this.precisionQueuesPool.findById(defaultQueue);
        }
        return queue;
    }
}
