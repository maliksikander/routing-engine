package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.services.controllerservices.PrecisionQueuesService;
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

/**
 * Rest-Controller for the Precision-Queues CRUD APIs.
 */
@RestController
public class PrecisionQueuesController {
    /**
     * The API calls are passed to this service for processing.
     */
    private final PrecisionQueuesService service;

    /**
     * Default Constructor. Loads the required dependency beans.
     *
     * @param service handles the actual processing for the API calls.
     */
    @Autowired
    public PrecisionQueuesController(PrecisionQueuesService service) {
        this.service = service;
    }

    /**
     * Create-Precision-Queue API handler. Creates a new Precision-Queue and sends back the API response.
     *
     * @param requestBody a PrecisionQueueEntity object
     * @return the newly created Precision-Queue as the response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/precision-queues", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> create(@Valid @RequestBody PrecisionQueueRequestBody requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    /**
     * Retrieve-Precision-Queues API handler. Returns the list of all PrecisionQueues in the config DB.
     *
     * @return list of all PrecisionQueues as response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/precision-queues", produces = "application/json")
    public ResponseEntity<Object> retrieve() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    /**
     * Update-Precision-Queue API handler. Updates an Existing precision-queue. Returns 404 Not-Found
     * if the requested precision-queue is not found.
     *
     * @param requestBody updated values of the Precision-Queue.
     * @param id          id of the precision-queue to update.
     * @return the updated Precision-Queue as response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @PutMapping(value = "/precision-queues/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> update(@Valid @RequestBody PrecisionQueueRequestBody requestBody,
                                         @PathVariable UUID id) {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    /**
     * Delete-Precision-queue API handler. Deletes an existing agent. Returns 404 if the requested
     * precision-queue is not found.
     *
     * @param id UUID of the precision-queue to be deleted
     * @return Success message response with status code 200
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/precision-queues/{id}", produces = "application/json")
    public ResponseEntity<Object> delete(@PathVariable UUID id) {
        return this.service.delete(id);
    }
}
