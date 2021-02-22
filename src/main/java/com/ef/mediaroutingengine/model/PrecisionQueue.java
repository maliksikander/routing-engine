package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "precisionQueues")
public class PrecisionQueue {

    @Id
    private UUID id;
    @NotBlank
    private String name;
    @NotNull
    private MediaRoutingDomain mrd;
    @NotNull
    private AgentSelectionCriteria agentSelectionCriteria;
    @NotNull
    private ServiceLevelType serviceLevelType;
    @NotNull
    private int serviceLevelThreshold;
    private List<Step> steps;

    public PrecisionQueue() {
        this.steps = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public ServiceLevelType getServiceLevelType() {
        return serviceLevelType;
    }

    public void setServiceLevelType(ServiceLevelType serviceLevelType) {
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

    public boolean containsStep(Step step) {
        return this.steps.contains(step);
    }

    public boolean addStep(Step step) {
        return this.steps.add(step);
    }

    public boolean removeStep(Step step) {
        return this.steps.remove(step);
    }

    @Override
    public String toString() {
        return "PrecisionQueue{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mrd=" + mrd +
                ", agentSelectionCriteria=" + agentSelectionCriteria +
                ", serviceLevelType=" + serviceLevelType +
                ", serviceLevelThreshold=" + serviceLevelThreshold +
                ", steps=" + steps +
                '}';
    }
}
