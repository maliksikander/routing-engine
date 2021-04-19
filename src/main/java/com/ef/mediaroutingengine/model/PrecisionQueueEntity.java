package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "precisionQueues")
public class PrecisionQueueEntity {

    @Id
    private UUID id;
    @NotNull
    @Size(min = 3, max = 50)
    private String name;
    @NotNull
    private MediaRoutingDomain mrd;
    @NotNull
    private AgentSelectionCriteria agentSelectionCriteria;
    @Min(1)
    private int serviceLevelType;
    @NotNull
    private int serviceLevelThreshold;
    @NotNull
    private List<StepEntity> steps;

    public PrecisionQueueEntity() {
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

    public List<StepEntity> getSteps() {
        return steps;
    }

    public void setSteps(List<StepEntity> stepEntities) {
        this.steps = stepEntities;
    }

    public boolean containsStep(StepEntity stepEntity) {
        return this.steps.contains(stepEntity);
    }

    public boolean addStep(StepEntity stepEntity) {
        return this.steps.add(stepEntity);
    }

    public boolean removeStep(StepEntity stepEntity) {
        return this.steps.remove(stepEntity);
    }

    @Override
    public String toString() {
        return "PrecisionQueue{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", mrd=" + mrd
                + ", agentSelectionCriteria=" + agentSelectionCriteria
                + ", serviceLevelType=" + serviceLevelType
                + ", serviceLevelThreshold=" + serviceLevelThreshold
                + ", steps=" + steps
                + '}';
    }
}
