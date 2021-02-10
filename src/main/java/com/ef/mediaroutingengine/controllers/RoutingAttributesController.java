package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.services.controllerservices.RoutingAttributesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
public class RoutingAttributesController {
    @Autowired
    private RoutingAttributesService service;

    @PostMapping(value = "/routing-attributes", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createRoutingAttribute(@Valid @RequestBody RoutingAttribute requestBody) {
        RoutingAttribute responseBody = this.service.create(requestBody);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @GetMapping(value = "/routing-attributes", produces = "application/json")
    public ResponseEntity<Object> retrieveRoutingAttributes() {
        List<RoutingAttribute> responseBody = this.service.retrieve();
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PutMapping(value = "/routing-attributes/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateRoutingAttribute(@Valid @RequestBody RoutingAttribute requestBody, @PathVariable UUID id) {
        this.service.update(requestBody, id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully updated");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @DeleteMapping(value = "/routing-attributes/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteRoutingAttribute(@PathVariable UUID id) {
        this.service.delete(id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
