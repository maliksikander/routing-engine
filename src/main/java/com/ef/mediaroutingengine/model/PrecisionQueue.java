package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrecisionQueue {
    private UUID id;
    private String name;
    private MediaRoutingDomain mrd;
    private String agentSelectionCriteria;
    private String serviceLevelType;
    private int serviceLevelThreshold;
    private List<Step> steps;

    public PrecisionQueue(){
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

    public String getAgentSelectionCriteria() {
        return agentSelectionCriteria;
    }

    public void setAgentSelectionCriteria(String agentSelectionCriteria) {
        this.agentSelectionCriteria = agentSelectionCriteria;
    }

    public String getServiceLevelType() {
        return serviceLevelType;
    }

    public void setServiceLevelType(String serviceLevelType) {
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

    public boolean containsStep(Step step){
        return this.steps.contains(step);
    }

    public boolean addStep(Step step){
        return this.steps.add(step);
    }

    public boolean removeStep(Step step){
        return this.steps.remove(step);
    }
}
