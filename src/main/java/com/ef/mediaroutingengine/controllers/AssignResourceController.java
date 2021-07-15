package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.services.controllerservices.AssignResourceService;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignResourceController {
    private final AssignResourceService assignResourceService;
    private final PrecisionQueuesPool precisionQueuesPool;

    @Autowired
    public AssignResourceController(AssignResourceService service,
                                    PrecisionQueuesPool precisionQueuesPool) {
        this.assignResourceService = service;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    /**
     * Assigns an agent to a conversation.
     *
     * @param request AssignResourceRequest
     * @return ResponseEntity
     */
    @PostMapping(value = "/assign-resource", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<String> assignResource(@RequestBody AssignResourceRequest request) {
        this.validate(request);
        CompletableFuture.runAsync(() -> this.assignResourceService.assign(request));
        return new ResponseEntity<>("The request is received Successfully", HttpStatus.OK);
    }

    private void validate(AssignResourceRequest request) {
        ChannelSession channelSession = request.getChannelSession();
        this.validateChannelSession(channelSession);

        UUID mrdId = channelSession.getChannel().getChannelConnector().getChannelType().getMediaRoutingDomain();
        if (mrdId == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConnector.ChannelType."
                    + "MediaRoutingDomain is null");
        }
        UUID defaultQueue = channelSession.getChannel().getChannelConfig().getRoutingPolicy().getDefaultQueue();
        UUID requestQueue = request.getQueue();
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
    }

    private void validateChannelSession(ChannelSession channelSession) {
        if (channelSession == null) {
            throw new IllegalArgumentException("Channel Session is null");
        }
        if (channelSession.getChannel() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel is null");
        }
        if (channelSession.getChannel().getChannelConnector() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConnector is null");
        }
        if (channelSession.getChannel().getChannelConnector().getChannelType() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConnector.ChannelType is null");
        }
        if (channelSession.getChannel().getChannelConfig() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConfig is null");
        }
        if (channelSession.getChannel().getChannelConfig().getRoutingPolicy() == null) {
            throw new IllegalArgumentException("ChannelSession.Channel.ChannelConfig.RoutingPolicy is null");
        }
    }

    private PrecisionQueue getPrecisionQueueFrom(UUID requestedQueue, UUID defaultQueue) {
        PrecisionQueue queue = this.precisionQueuesPool.findById(requestedQueue);
        // If requested queue not found, use default queue
        if (queue == null) {
            queue = this.precisionQueuesPool.findById(defaultQueue);
        }
        return queue;
    }
}
