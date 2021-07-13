package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.services.controllerservices.PrecisionQueueEntityService;
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
public class PrecisionQueueEntityController {
    private final PrecisionQueueEntityService service;

    @Autowired
    public PrecisionQueueEntityController(PrecisionQueueEntityService service) {
        this.service = service;
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/precision-queues", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPrecisionQueue(
            @Valid @RequestBody PrecisionQueueEntity requestBody) throws Exception {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/precision-queues", produces = "application/json")
    public ResponseEntity<Object> retrievePrecisionQueues() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(value = "/precision-queues/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updatePrecisionQueue(
            @Valid @RequestBody PrecisionQueueEntity requestBody, @PathVariable UUID id) throws Exception {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    /**
     * Deletes a precision queue.
     *
     * @param id UUID
     * @return ResponseEntity
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/precision-queues/{id}", produces = "application/json")
    public ResponseEntity<Object> deletePrecisionQueue(@PathVariable UUID id) throws Exception {
        this.service.delete(id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
