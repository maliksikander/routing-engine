package com.ef.mediaroutingengine.routing.controller;

import com.ef.mediaroutingengine.global.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.service.PrecisionQueuesService;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
 * Rest-Controller for the Precision-Queues CRUD APIs.
 */
@Validated
@RestController
public class PrecisionQueuesController {

    private static final Logger logger = LoggerFactory.getLogger(PrecisionQueuesController.class);

    /**
     * The API calls are passed to this service for processing.
     */
    private final PrecisionQueuesService service;
    private final AgentsPool agentsPool;

    /**
     * Default Constructor. Loads the required dependency beans.
     *
     * @param service handles the actual processing for the API calls.
     */
    @Autowired
    public PrecisionQueuesController(PrecisionQueuesService service, AgentsPool agentsPool) {
        this.service = service;
        this.agentsPool = agentsPool;
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
    public ResponseEntity<Object> retrieve(@RequestParam(required = false, value = "queueId") String queueId) {
        return this.service.retrieve(queueId);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/precision-queues/name/{queueName}", produces = "application/json")
    public ResponseEntity<Object> retrieveByName(@PathVariable String queueName) {
        return ResponseEntity.ok().body(this.service.retrieveByName(queueName));
    }

    /**
     * Retrieves-Precision-Queues API handler. Returns the list of all PrecisionQueues with
     * the associated available agents.
     *
     * @param conversationId id of the conversation
     * @return returns the object for the queue with available agents
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "precision-queues/available-agents", produces = "application/json")
    public ResponseEntity<Object> retrieveQueuesWithAvailableAgents(@RequestParam @NotBlank String conversationId,
                                                                    @RequestParam @NotBlank String agentId) {
        logger.info("Request to receive queues with available agents on conversation: {} for agent: {}",
                conversationId, agentId);

        Agent agent = this.agentsPool.findBy(agentId);

        if (agent == null) {
            String errorMsg = "No agent found with id: " + agentId;
            logger.error(errorMsg);
            throw new NotFoundException(errorMsg);
        }

        return ResponseEntity.ok().body(this.service.getQueuesWithAvailableAgents(conversationId, agent));
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
                                         @PathVariable String id) {
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
    public ResponseEntity<Object> delete(@PathVariable String id) {
        return this.service.delete(id);
    }

    /**
     * To flush all tasks in a single queue.
     *
     * @param queueName     the queue name
     * @param enqueuedSince the time in seconds since the task is enqueued.
     * @return response object
     */
    @RolesAllowed({"client-admin", "client-agent", "client-supervisor"})
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = {"precision-queues/{queueName}/tasks"}, produces = "application/json")
    public ResponseEntity<Object> flush(@PathVariable String queueName,
                                        @RequestParam(defaultValue = "0") int enqueuedSince) {
        return ResponseEntity.ok().body(new SuccessResponseBody(this.service.flushBy(queueName, enqueuedSince)));
    }

    /**
     * To flush all tasks in all queues.
     *
     * @param enqueuedSince the time in seconds since the task is enqueued.
     * @return response object
     */
    @RolesAllowed({"client-admin", "client-agent", "client-supervisor"})
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = {"precision-queues/tasks"}, produces = "application/json")
    public ResponseEntity<Object> flush(@RequestParam(defaultValue = "0") int enqueuedSince) {
        return ResponseEntity.ok().body(new SuccessResponseBody(this.service.flushAll(enqueuedSince)));
    }


    /**
     * Request to get all the associated agents to queue.
     *
     * @param queueId the queue id.
     * @return the response object
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "precision-queues/associated-agents")
    public ResponseEntity<Object> getAssociatedAgents(@RequestParam(value = "queueId") Optional<String> queueId) {
        return queueId.<ResponseEntity<Object>>map(id ->
                        ResponseEntity.ok().body(this.service.getAssociatedAgentsOf(id)))
                .orElseGet(() -> ResponseEntity.ok().body(this.service.getAllAssociatedAgents()));
    }
}
