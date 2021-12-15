package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.utilities.TaskManager;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param taskManager         the task manager
     * @param precisionQueuesPool the precision queues pool
     * @param mrdPool             the mrd pool
     */
    @Autowired
    public AssignResourceServiceImpl(TaskManager taskManager,
                                     PrecisionQueuesPool precisionQueuesPool,
                                     MrdPool mrdPool) {
        this.taskManager = taskManager;
        this.precisionQueuesPool = precisionQueuesPool;
        this.mrdPool = mrdPool;
    }

    @Override
    public String assign(AssignResourceRequest request) {
        logger.debug(Constants.METHOD_STARTED);
        logger.info("Assign resource request initiated | Topic: {}", request.getChannelSession().getTopicId());

        ChannelSession channelSession = request.getChannelSession();
        validateChannelSession(channelSession);
        logger.debug("ChannelSession validated in Assign-Resource API request");

        MediaRoutingDomain mrd = validateAndGetMrd(channelSession);
        logger.debug("MRD validated in Assign-Resource API request");

        PrecisionQueue queue = this.validateAndGetQueue(channelSession, request.getQueue(), mrd.getId());
        logger.debug("PrecisionQueue validated in Assign-Resource API request");

        // TODO: Executor service .. don't use completableFuture!
        CompletableFuture.runAsync(() -> this.taskManager.enqueueTask(channelSession, queue, mrd));

        logger.info("Assign resource request handled gracefully | Topic: {}", request.getChannelSession().getTopicId());
        logger.debug(Constants.METHOD_ENDED);
        return "The request is received Successfully";
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
     * Validate and get mrd media routing domain.
     *
     * @param channelSession the channel session
     * @return the media routing domain
     */
    MediaRoutingDomain validateAndGetMrd(ChannelSession channelSession) {
        String mrdId = channelSession.getChannel().getChannelType().getMediaRoutingDomain();
        MediaRoutingDomain mrd = this.mrdPool.findById(mrdId);
        if (mrd == null) {
            throw new IllegalArgumentException("MRD with id: " + mrdId + " not found in MRD-pool");
        }
        return mrd;
    }

    /**
     * Validate and get queue precision queue.
     *
     * @param channelSession the channel session
     * @param requestQueue   the request queue
     * @param mrdId          the mrd id
     * @return the precision queue
     */
    PrecisionQueue validateAndGetQueue(ChannelSession channelSession, String requestQueue, String mrdId) {
        String defaultQueue = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getRoutingObjectId();
        if (defaultQueue == null && requestQueue == null) {
            throw new IllegalArgumentException("DefaultQueue and RequestedQueue both are null");
        }
        PrecisionQueue queue = getPrecisionQueueFrom(requestQueue, defaultQueue);
        if (queue == null) {
            throw new IllegalArgumentException("Could not find PrecisionQueue for this request");
        }
        if (!queue.getMrd().getId().equals(mrdId)) {
            throw new IllegalArgumentException("The requested MRD is not associated with the "
                    + "requested PrecisionQueue");
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
    PrecisionQueue getPrecisionQueueFrom(String requestedQueue, String defaultQueue) {
        PrecisionQueue queue = this.precisionQueuesPool.findById(requestedQueue);
        // If requested queue not found, use default queue
        if (queue == null) {
            queue = this.precisionQueuesPool.findById(defaultQueue);
        }
        return queue;
    }
}
