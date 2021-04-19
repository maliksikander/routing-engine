package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import java.util.List;

public class RoutingAttributeDeleteConflictResponse {

    private List<PrecisionQueueEntity> precisionQueueEntities;
    private List<CCUser> agents;

    public List<PrecisionQueueEntity> getPrecisionQueues() {
        return precisionQueueEntities;
    }

    public void setPrecisionQueues(List<PrecisionQueueEntity> precisionQueueEntities) {
        this.precisionQueueEntities = precisionQueueEntities;
    }

    public List<CCUser> getAgents() {
        return agents;
    }

    public void setAgents(List<CCUser> agents) {
        this.agents = agents;
    }
}
