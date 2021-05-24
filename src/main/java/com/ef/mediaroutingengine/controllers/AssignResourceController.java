package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.services.controllerservices.AssignResourceService;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssignResourceController {
    private final AssignResourceService assignResourceService;

    @Autowired
    public AssignResourceController(AssignResourceService service) {
        this.assignResourceService = service;
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
        CompletableFuture.runAsync(() -> this.assignResourceService.assign(request));
        return new ResponseEntity<>("The request is received Successfully", HttpStatus.OK);
    }
}
