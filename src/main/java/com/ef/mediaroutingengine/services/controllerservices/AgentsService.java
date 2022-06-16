package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.CCUser;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

/**
 * The interface Agents service.
 */
public interface AgentsService {

    /**
     * Create cc user.
     *
     * @param agent the agent
     * @return the cc user
     */
    CCUser create(CCUser agent);

    /**
     * Retrieve list.
     *
     * @return the list
     */
    List<CCUser> retrieve();

    /**
     * Update cc user.
     *
     * @param agent the agent
     * @param id    the id
     * @return the cc user
     */
    ResponseEntity<Object> update(CCUser agent, UUID id);

    /**
     * Delete.
     *
     * @param id the id
     * @return the response entity
     */
    ResponseEntity<Object> delete(UUID id);
}
