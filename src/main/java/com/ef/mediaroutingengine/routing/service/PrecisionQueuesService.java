package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.mediaroutingengine.routing.dto.AssociatedAgentsResponse;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import java.util.List;
import org.springframework.http.ResponseEntity;

/**
 * The interface Precision queue entity service.
 */
public interface PrecisionQueuesService {

    /**
     * Create precision queue entity.
     *
     * @param requestBody the precision queue entity
     * @return the precision queue entity
     */
    PrecisionQueueEntity create(PrecisionQueueRequestBody requestBody);

    /**
     * Retrieve list.
     *
     * @param queueId the queue id
     * @return the list
     */
    ResponseEntity<Object> retrieve(String queueId);

    /**
     * Update precision queue entity.
     *
     * @param requestBody the precision queue entity
     * @param id          the id
     * @return the precision queue entity
     */
    PrecisionQueueEntity update(PrecisionQueueRequestBody requestBody, String id);

    /**
     * Delete.
     *
     * @param id the id
     * @return the response entity
     */
    ResponseEntity<Object> delete(String id);

    /**
     * Retrieve precision queue with available agents.
     *
     * @param conversationId id for the conversation
     * @return the precision queue with available agents
     */
    ResponseEntity<Object> retrieveQueuesWithAssociatedAvailableAgents(String conversationId);

    /**
     * Flush the task(s) in queue(s).
     *
     * @param queueName     name of the queue o be flushed.
     * @param enqueuedSince time in seconds since the task is enqueued
     * @return the execution status.
     */
    String flushBy(String queueName, int enqueuedSince);

    /**
     * Flush all string.
     *
     * @param enqueuedSince time in seconds since the task is enqueued
     * @return the string
     */
    String flushAll(int enqueuedSince);

    /**
     * Returns the associated agents to a queue.
     *
     * @param queueId the queue id.
     * @return the response object
     */
    AssociatedAgentsResponse getAssociatedAgentsOf(String queueId);

    /**
     * Returns the associated agents of all queues.
     *
     * @return the response.
     */
    List<AssociatedAgentsResponse> getAllAssociatedAgents();
}