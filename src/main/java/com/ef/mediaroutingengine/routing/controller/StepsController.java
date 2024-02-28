package com.ef.mediaroutingengine.routing.controller;

import com.ef.cim.objectmodel.StepEntity;
import com.ef.mediaroutingengine.routing.service.StepsService;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
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
                                         @PathVariable String queueId, @PathVariable String id) {
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
    public ResponseEntity<Object> delete(@PathVariable String queueId, @PathVariable String id) {
        return this.service.delete(queueId, id);
    }

    /**
     * This API retrieves list of agents who are falling in step criteria in queue (X).
     *
     * @param queueId queueId
     * @return Agents list.
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/precision-queues/{queueId}/agents")
    public ResponseEntity<Object> getAgentsMatchingStepsCriteria(@PathVariable String queueId,
                                                                 @RequestParam(required = false)
                                                                 Optional<String> stepId) {
        return new ResponseEntity<>(this.service.previewAgentsMatchingStepCriteriaInQueue(queueId, stepId),
                HttpStatus.OK);
    }
}
