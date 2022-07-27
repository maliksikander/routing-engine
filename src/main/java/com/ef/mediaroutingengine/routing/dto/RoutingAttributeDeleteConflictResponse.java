package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import java.util.List;

/**
 * The type Routing attribute delete conflict response.
 */
public class RoutingAttributeDeleteConflictResponse {

    /**
     * The Precision queue entities.
     */
    private List<PrecisionQueueEntity> precisionQueueEntities;
    /**
     * The Agents.
     */
    private List<CCUser> agents;

    /**
     * Gets precision queues.
     *
     * @return the precision queues
     */
    public List<PrecisionQueueEntity> getPrecisionQueues() {
        return precisionQueueEntities;
    }

    /**
     * Sets precision queues.
     *
     * @param precisionQueueEntities the precision queue entities
     */
    public void setPrecisionQueues(List<PrecisionQueueEntity> precisionQueueEntities) {
        this.precisionQueueEntities = precisionQueueEntities;
    }

    /**
     * Gets agents.
     *
     * @return the agents
     */
    public List<CCUser> getAgents() {
        return agents;
    }

    /**
     * Sets agents.
     *
     * @param agents the agents
     */
    public void setAgents(List<CCUser> agents) {
        this.agents = agents;
    }
}
