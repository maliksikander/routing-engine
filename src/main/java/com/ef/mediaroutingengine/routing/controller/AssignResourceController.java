package com.ef.mediaroutingengine.routing.controller;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.RoutingMode;
import com.ef.cim.objectmodel.RoutingPolicy;
import com.ef.cim.objectmodel.dto.AssignResourceRequest;
import com.ef.cim.objectmodel.dto.RequestQueue;
import com.ef.cim.objectmodel.task.TaskType;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import com.ef.mediaroutingengine.routing.pool.PrecisionQueuesPool;
import com.ef.mediaroutingengine.routing.service.AssignResourceService;
import java.util.HashMap;
import javax.validation.Valid;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest-Controller for the Assign-Resource API.
 */
@RestController
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignResourceController {

    /**
     * The API calls are passed to this service for processing.
     */
    private final AssignResourceService assignResourceService;
    private final PrecisionQueuesPool precisionQueuesPool;

    /**
     * Default Constructor. Loads the required beans.
     *
     * @param service handles the actual processing for the API calls.
     */
    @Autowired
    public AssignResourceController(AssignResourceService service, PrecisionQueuesPool precisionQueuesPool) {
        this.assignResourceService = service;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    /**
     * Assigns an agent to a conversation.
     *
     * @param request AssignResourceRequest
     * @return ResponseEntity response entity
     */
    @PostMapping(value = "/assign-resource", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<String> assignResource(@Valid @RequestBody AssignResourceRequest request) {
        MDC.put(Constants.MDC_TOPIC_ID, request.getRequestSession().getConversationId());

        this.validate(request);

        PrecisionQueue queue = this.getQueue(request);
        this.validateQueue(queue);

        if (request.getType().getMetadata() == null) {
            request.getType().setMetadata(new HashMap<>());
        }
        request.getType().putMetadata("offerToAgent", request.isOfferToAgent());

        this.assignResourceService.assign(request, queue);
        return ResponseEntity.ok().body("Request Received Successfully");
    }

    private void validate(AssignResourceRequest req) {
        TaskType requestType = req.getType();

        if (requestType.getMode() == null || requestType.getMode() != Enums.TaskTypeMode.QUEUE) {
            throw new IllegalArgumentException("Invalid request mode, it should be QUEUE");
        }

        RoutingMode mode = req.getRequestSession().getChannel().getChannelConfig().getRoutingPolicy().getRoutingMode();

        if (requestType.getDirection().equals(Enums.TaskTypeDirection.INBOUND) && !mode.equals(RoutingMode.PUSH)) {
            throw new IllegalArgumentException("Routing mode must be PUSH for an INBOUND request");
        }
    }

    private PrecisionQueue getQueue(AssignResourceRequest req) {
        RequestQueue requestQueue = req.getQueue();

        // Use default queue if no queue supplied in request.
        if (requestQueue == null) {
            RoutingPolicy routingPolicy = req.getRequestSession().getChannel().getChannelConfig().getRoutingPolicy();
            String defaultQueueId = routingPolicy.getRoutingObjectId();
            return this.precisionQueuesPool.findById(defaultQueueId);
        }

        if (requestQueue.getType().equals(RequestQueue.Type.ID)) {
            return this.precisionQueuesPool.findById(requestQueue.getValue());
        } else {
            return this.precisionQueuesPool.findByName(requestQueue.getValue());
        }
    }

    private void validateQueue(PrecisionQueue queue) {
        if (queue == null) {
            throw new IllegalArgumentException("Could not find PrecisionQueue for this request");
        }
        if (queue.getSteps().isEmpty()) {
            throw new IllegalStateException("Cannot process request, Queue: " + queue.getId()
                    + " has no steps configured");
        }
    }
}
