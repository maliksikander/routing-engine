package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.services.controllerservices.AssignResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class AssignResourceController {
    @Autowired
    AssignResourceService assignResourceService;

    @PostMapping(value = "/routingEngine/assignResource", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> assignResource(@RequestBody AssignResourceRequest request){
        CompletableFuture.runAsync(() -> {
            this.assignResourceService.assign(request);
        });
        return new ResponseEntity<>("The request is received Successfully", HttpStatus.OK);
    }
}
