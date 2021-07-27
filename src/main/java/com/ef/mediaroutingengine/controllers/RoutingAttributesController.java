package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.RoutingAttributeDeleteConflictResponse;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.services.controllerservices.RoutingAttributesService;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Rest-Controller for the Routing-Attributes CRUD APIs.
 */
@RestController
public class RoutingAttributesController {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingAttributesController.class);
    /**
     * The API calls are passed to this service for processing.
     */
    private final RoutingAttributesService service;

    /**
     * Default Constructor. Loads the required dependency beans.
     *
     * @param service handles the actual processing for the API calls.
     */
    @Autowired
    public RoutingAttributesController(RoutingAttributesService service) {
        this.service = service;
    }

    /**
     * Create-Routing-Attribute API handler. Creates a new Routing-Attribute and sends back the API response.
     *
     * @param requestBody a RoutingAttribute object
     * @return the newly created RoutingAttribute as the response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/routing-attributes", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createRoutingAttribute(
            @Valid @RequestBody RoutingAttribute requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    /**
     * Retrieve-Routing-Attributes API handler. Returns the list of all Routing-Attributes in the config DB.
     *
     * @return list of all Routing-Attributes as response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/routing-attributes", produces = "application/json")
    public ResponseEntity<Object> retrieveRoutingAttributes() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    /**
     * Updates-Routing-Attribute API handler. Updates an Existing Routing-Attribute. Returns 404 Not-Found if the
     * requested Routing-Attribute is not found.
     *
     * @param requestBody updated value of the Routing-Attribute.
     * @param id          id of the Routing-Attribute to update.
     * @return the updated Routing-Attribute as response-body with status-code 200.
     * @throws Exception In case an agent is not found or depended data is inconsistent.
     */
    @CrossOrigin(origins = "*")
    @PutMapping(value = "/routing-attributes/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateRoutingAttribute(
            @Valid @RequestBody RoutingAttribute requestBody, @PathVariable UUID id) throws Exception {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    /**
     * Delete-Routing-Attribute API handler. Deletes an existing Routing-Attribute. Returns 404 if the requested
     * Routing-Attribute is not found. Returns HttpStatus-Conflict without deleting the Routing-Attribute if the
     * Routing-Attribute is associated to any precision-queue's step or any Agent. In this case the list of
     * precision-queues and list of Agents to which the Routing-Attribute is associated is returned as response-body.
     *
     * @param id UUID of the Routing-Attribute to be deleted
     * @return 200 on successful deletion, 404 if not found, 409 if Routing-Attribute is associated to other entities.
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/routing-attributes/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteRoutingAttribute(@PathVariable UUID id) {
        RoutingAttributeDeleteConflictResponse deleteConflictResponse = this.service.delete(id);
        if (deleteConflictResponse != null) {
            LOGGER.debug("Could not delete Routing-Attribute: {}. It is associated with one or more Queues or"
                    + "Agents", id);
            return new ResponseEntity<>(deleteConflictResponse, HttpStatus.CONFLICT);
        }
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
