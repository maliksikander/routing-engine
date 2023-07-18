package com.ef.mediaroutingengine.routing.controller;

import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.routing.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.routing.service.AssignResourceService;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * Default Constructor. Loads the required beans.
     *
     * @param service handles the actual processing for the API calls.
     */
    @Autowired
    public AssignResourceController(AssignResourceService service) {
        this.assignResourceService = service;
    }

    /**
     * Assigns an agent to a conversation.
     *
     * @param request AssignResourceRequest
     * @return ResponseEntity response entity
     */
    @PostMapping(value = "/assign-resource", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<String> assignResource(@RequestBody AssignResourceRequest request,
                                                 @RequestParam Optional<Boolean> queueName,
                                                 @RequestParam Optional<Boolean> offerToAgent,
                                                 @RequestParam(defaultValue = "1", required = false) int priority) {
        MDC.put(Constants.MDC_TOPIC_ID, request.getChannelSession().getConversationId());

        if (priority > 10) {
            priority = 10;
        } else if (priority < 1) {
            priority = 1;
        }

        return ResponseEntity.ok().body(this.assignResourceService.assign(request, queueName.orElse(false),
                offerToAgent.orElse(true), priority));
    }
}
