package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.PrecisionQueue;
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

@RestController
public class PrecisionQueuesController {

    @Autowired
    PrecisionQueuesService service;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/precision-queues", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createPrecisionQueue(
            @Valid @RequestBody PrecisionQueue requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "/precision-queues", produces = "application/json")
    public ResponseEntity<Object> retrievePrecisionQueues() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "/precision-queues/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updatePrecisionQueue(
            @Valid @RequestBody PrecisionQueue requestBody, @PathVariable UUID id) {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping(value = "/precision-queues/{id}", produces = "application/json")
    public ResponseEntity<Object> deletePrecisionQueue(@PathVariable UUID id) {
        this.service.delete(id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
