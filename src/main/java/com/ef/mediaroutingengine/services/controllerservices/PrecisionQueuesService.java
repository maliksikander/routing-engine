package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
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
     * @return the list
     */
    List<PrecisionQueueEntity> retrieve();

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
}
