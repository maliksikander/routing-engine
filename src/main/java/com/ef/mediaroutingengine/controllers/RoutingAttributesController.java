package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.RoutingAttributeDeleteConflictResponse;
import com.ef.mediaroutingengine.services.controllerservices.RoutingAttributesService;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoutingAttributesController {
    private final RoutingAttributesService service;

    @Autowired
    public RoutingAttributesController(RoutingAttributesService service) {
        this.service = service;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/routing-attributes", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createRoutingAttribute(
            @Valid @RequestBody RoutingAttribute requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "/routing-attributes", produces = "application/json")
    public ResponseEntity<Object> retrieveRoutingAttributes() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "/routing-attributes/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateRoutingAttribute(
            @Valid @RequestBody RoutingAttribute requestBody, @PathVariable UUID id) {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    /**
     * Deletes a routing attribute.
     *
     * @param id UUID
     * @return ResponseEntity
     */
    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping(value = "/routing-attributes/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteRoutingAttribute(@PathVariable UUID id) {
        RoutingAttributeDeleteConflictResponse deleteConflictResponse = this.service.delete(id);
        if (deleteConflictResponse != null) {
            return new ResponseEntity<>(deleteConflictResponse, HttpStatus.CONFLICT);
        }
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
