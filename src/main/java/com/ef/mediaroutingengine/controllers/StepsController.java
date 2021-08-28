package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.StepEntity;
import com.ef.mediaroutingengine.services.controllerservices.StepsService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StepsController {
    private final StepsService service;

    @Autowired
    public StepsController(StepsService service) {
        this.service = service;
    }

    @PostMapping("/precision-queues/{queueId}/steps")
    public ResponseEntity<Object> create(@RequestBody StepEntity stepEntity, @PathVariable UUID queueId) {
        return new ResponseEntity<>(this.service.create(queueId, stepEntity), HttpStatus.OK);
    }

    @PutMapping("/precision-queues/{queueId}/steps/{id}")
    public ResponseEntity<Object> update(@RequestBody StepEntity stepEntity,
                                         @PathVariable UUID queueId, @PathVariable UUID id) {
        return new ResponseEntity<>(this.service.update(id, queueId, stepEntity), HttpStatus.OK);
    }

    @DeleteMapping("/precision-queues/{queueId}/steps/{id}")
    public ResponseEntity<Object> delete(@PathVariable UUID queueId, @PathVariable UUID id) {
        return this.service.delete(queueId, id);
    }
}
