package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import java.util.List;
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
    CCUser createOrUpdate(CCUser agent);

    void createOrUpdate(KeycloakUser keycloakUser);

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
    ResponseEntity<Object> update(CCUser agent, String id);

    void update(KeycloakUser keycloakUser);

    /**
     * Delete.
     *
     * @param id the id
     * @return the response entity
     */
    ResponseEntity<Object> delete(String id);
}
