package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.RoutingAttributeDeleteConflictResponse;
import com.ef.mediaroutingengine.services.controllerservices.RoutingAttributesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
public class RoutingAttributesController {
    @Autowired
    private RoutingAttributesService service;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/routing-attributes", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createRoutingAttribute(@Valid @RequestBody RoutingAttribute requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "/routing-attributes", produces = "application/json")
    public ResponseEntity<Object> retrieveRoutingAttributes() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "/routing-attributes/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateRoutingAttribute(@Valid @RequestBody RoutingAttribute requestBody, @PathVariable UUID id) {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping(value = "/routing-attributes/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteRoutingAttribute(@PathVariable UUID id) {
        RoutingAttributeDeleteConflictResponse deleteConflictResponse = this.service.delete(id);
        if(deleteConflictResponse!=null){
            return new ResponseEntity<>(deleteConflictResponse, HttpStatus.CONFLICT);
        }
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
