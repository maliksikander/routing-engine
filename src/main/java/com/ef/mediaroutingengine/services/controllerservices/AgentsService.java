package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.CCUser;
import java.util.List;
import java.util.UUID;

/**
 * The interface Agents service.
 */
public interface AgentsService {

    /**
     * Create cc user.
     *
     * @param agent the agent
     * @return the cc user
     * @throws Exception the exception
     */
    CCUser create(CCUser agent) throws Exception;

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
     * @throws Exception the exception
     */
    CCUser update(CCUser agent, UUID id) throws Exception;

    /**
     * Delete.
     *
     * @param id the id
     * @throws Exception the exception
     */
    void delete(UUID id) throws Exception;
}
