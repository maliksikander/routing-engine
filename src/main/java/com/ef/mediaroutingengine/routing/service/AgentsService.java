package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.AgentPresence;
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

    /**
     * Create or update.
     *
     * @param keycloakUser the keycloak user
     */
    void createOrUpdate(KeycloakUser keycloakUser);

    /**
     * Retrieve list.
     *
     * @return the list
     */
    List<CCUser> retrieve();

    /**
     * This retrieves all the agents with their states.
     *
     * @return agents list with their state
     */
    List<AgentPresence> retrieveAgentsWithStates();

    /**
     * Update cc user.
     *
     * @param agent the agent
     * @param id    the id
     * @return the cc user
     */
    ResponseEntity<Object> update(CCUser agent, String id);

    /**
     * Update.
     *
     * @param keycloakUser the keycloak user
     */
    void update(KeycloakUser keycloakUser);

    /**
     * Delete.
     *
     * @param id the id
     * @return the response entity
     */
    ResponseEntity<Object> delete(String id);
}
