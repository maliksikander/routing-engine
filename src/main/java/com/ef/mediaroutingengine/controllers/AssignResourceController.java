package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.services.controllerservices.AssignResourceService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> assignResource(@RequestBody AssignResourceRequest request) {
        MDC.put(Constants.MDC_TOPIC_ID, request.getChannelSession().getConversationId().toString());
        return new ResponseEntity<>(this.assignResourceService.assign(request), HttpStatus.OK);
    }
}
