package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.StepEntity;
import com.ef.mediaroutingengine.services.controllerservices.StepsService;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Steps controller.
 */
@RestController
public class StepsController {
    /**
     * The Service.
     */
    private final StepsService service;

    /**
     * Instantiates a new Steps controller.
     *
     * @param service the service
     */
    @Autowired
    public StepsController(StepsService service) {
        this.service = service;
    }

    /**
     * Create response entity.
     *
     * @param stepEntity the step entity
     * @param queueId    the queue id
     * @return the response entity
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/precision-queues/{queueId}/steps")
    public ResponseEntity<Object> create(@Valid @RequestBody StepEntity stepEntity, @PathVariable String queueId) {
        return new ResponseEntity<>(this.service.create(queueId, stepEntity), HttpStatus.OK);
    }

    /**
     * Update response entity.
     *
     * @param stepEntity the step entity
     * @param queueId    the queue id
     * @param id         the id
     * @return the response entity
     */
    @CrossOrigin(origins = "*")
    @PutMapping("/precision-queues/{queueId}/steps/{id}")
    public ResponseEntity<Object> update(@Valid @RequestBody StepEntity stepEntity,
                                         @PathVariable String queueId, @PathVariable UUID id) {
        return new ResponseEntity<>(this.service.update(id, queueId, stepEntity), HttpStatus.OK);
    }

    /**
     * Delete response entity.
     *
     * @param queueId the queue id
     * @param id      the id
     * @return the response entity
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping("/precision-queues/{queueId}/steps/{id}")
    public ResponseEntity<Object> delete(@PathVariable String queueId, @PathVariable UUID id) {
        return this.service.delete(queueId, id);
    }
}
