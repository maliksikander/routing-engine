package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
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
}
