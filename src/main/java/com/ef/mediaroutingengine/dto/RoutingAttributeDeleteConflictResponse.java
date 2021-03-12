package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import java.util.List;

public class RoutingAttributeDeleteConflictResponse {

    private List<PrecisionQueue> precisionQueues;
    private List<CCUser> agents;

    public List<PrecisionQueue> getPrecisionQueues() {
        return precisionQueues;
    }

    public void setPrecisionQueues(List<PrecisionQueue> precisionQueues) {
        this.precisionQueues = precisionQueues;
    }

    public List<CCUser> getAgents() {
        return agents;
    }

    public void setAgents(List<CCUser> agents) {
        this.agents = agents;
    }
}
