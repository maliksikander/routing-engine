package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.CCUser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrecisionQueue {
    private UUID id;
    private String name;
    private MediaRoutingDomain mrd;
    private AgentSelectionCriteria agentSelectionCriteria;
    private int serviceLevelType;
    private int serviceLevelThreshold;
    private List<Step> steps;

    public PrecisionQueue() {

    }

    /**
     * Parametrized constructor. Constructs a PrecisionQueue object with a PrecisionQueueEntity object.
     *
     * @param pqEntity the precision-queue entity object Stored in the DB.
     */
    public PrecisionQueue(PrecisionQueueEntity pqEntity) {
        this.id = pqEntity.getId();
        this.name = pqEntity.getName();
        this.mrd = pqEntity.getMrd();
        this.agentSelectionCriteria = pqEntity.getAgentSelectionCriteria();
        this.serviceLevelType = pqEntity.getServiceLevelType();
        this.serviceLevelThreshold = pqEntity.getServiceLevelThreshold();
        this.steps = toSteps(pqEntity.getSteps());
    }

    private List<Step> toSteps(List<StepEntity> stepEntities) {
        if (stepEntities == null) {
            return new ArrayList<>();
        }
        List<Step> elements = new ArrayList<>();
        for (StepEntity stepEntity: stepEntities) {
            elements.add(new Step(stepEntity));
        }
        return elements;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Sets the id only if it is null.
     *
     * @param id unique id to set
     */
    public void setId(UUID id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    public AgentSelectionCriteria getAgentSelectionCriteria() {
        return agentSelectionCriteria;
    }

    public void setAgentSelectionCriteria(AgentSelectionCriteria agentSelectionCriteria) {
        this.agentSelectionCriteria = agentSelectionCriteria;
    }

    public int getServiceLevelType() {
        return serviceLevelType;
    }

    public void setServiceLevelType(int serviceLevelType) {
        this.serviceLevelType = serviceLevelType;
    }

    public int getServiceLevelThreshold() {
        return serviceLevelThreshold;
    }

    public void setServiceLevelThreshold(int serviceLevelThreshold) {
        this.serviceLevelThreshold = serviceLevelThreshold;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    /**
     * Evaluates the agents associated with each step in this precision-queue.
     *
     * @param allAgents List of all agents in the configuration DB
     */
    public void evaluateAgentsAssociatedWithSteps(List<CCUser> allAgents) {
        if (steps == null) {
            return;
        }
        for (Step step: steps) {
            step.evaluateAssociatedAgents(allAgents);
        }
    }
}
