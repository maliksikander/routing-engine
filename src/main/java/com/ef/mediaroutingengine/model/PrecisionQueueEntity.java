package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.dto.PrecisionQueueRequestBody;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The type Precision queue entity.
 */
@Document(value = "precisionQueues")
public class PrecisionQueueEntity {
    /**
     * The Id.
     */
    @Id
    private String id;
    /**
     * The Name.
     */
    @NotNull
    @Size(min = 3, max = 50)
    private String name;
    /**
     * The Mrd.
     */
    @NotNull
    private MediaRoutingDomain mrd;
    /**
     * The Service level type.
     */
    @Min(1)
    private int serviceLevelType;
    /**
     * The Service level threshold.
     */
    @NotNull
    private int serviceLevelThreshold;
    /**
     * The Steps.
     */
    @NotNull
    private List<StepEntity> steps;

    /**
     * Instantiates a new Precision queue entity.
     */
    public PrecisionQueueEntity() {
        this.steps = new ArrayList<>();
    }

    /**
     * Instantiates a new Precision queue entity.
     *
     * @param requestBody the request body
     */
    public PrecisionQueueEntity(PrecisionQueueRequestBody requestBody) {
        this.name = requestBody.getName();
        this.mrd = requestBody.getMrd();
        this.serviceLevelType = requestBody.getServiceLevelType();
        this.serviceLevelThreshold = requestBody.getServiceLevelThreshold();
        this.steps = new ArrayList<>();
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets mrd.
     *
     * @return the mrd
     */
    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    /**
     * Sets mrd.
     *
     * @param mrd the mrd
     */
    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    /**
     * Gets service level type.
     *
     * @return the service level type
     */
    public int getServiceLevelType() {
        return serviceLevelType;
    }

    /**
     * Sets service level type.
     *
     * @param serviceLevelType the service level type
     */
    public void setServiceLevelType(int serviceLevelType) {
        this.serviceLevelType = serviceLevelType;
    }

    /**
     * Gets service level threshold.
     *
     * @return the service level threshold
     */
    public int getServiceLevelThreshold() {
        return serviceLevelThreshold;
    }

    /**
     * Sets service level threshold.
     *
     * @param serviceLevelThreshold the service level threshold
     */
    public void setServiceLevelThreshold(int serviceLevelThreshold) {
        this.serviceLevelThreshold = serviceLevelThreshold;
    }

    /**
     * Gets steps.
     *
     * @return the steps
     */
    public List<StepEntity> getSteps() {
        return steps;
    }

    /**
     * Sets steps.
     *
     * @param stepEntities the step entities
     */
    public void setSteps(List<StepEntity> stepEntities) {
        this.steps = stepEntities;
    }

    /**
     * Contains step boolean.
     *
     * @param stepEntity the step entity
     * @return the boolean
     */
    public boolean containsStep(StepEntity stepEntity) {
        return this.steps.contains(stepEntity);
    }

    /**
     * Contains step boolean.
     *
     * @param stepId the step id
     * @return the boolean
     */
    public boolean containsStep(UUID stepId) {
        for (StepEntity step : this.steps) {
            if (step.getId().equals(stepId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add step boolean.
     *
     * @param stepEntity the step entity
     */
    public void addStep(StepEntity stepEntity) {
        if (this.steps.size() >= 10) {
            throw new IllegalStateException("Only 10 steps are allowed on this queue");
        }
        if (stepEntity != null) {
            this.steps.add(stepEntity);
        }
    }

    /**
     * Delete step.
     *
     * @param stepEntity the step entity
     */
    public void deleteStep(StepEntity stepEntity) {
        this.steps.remove(stepEntity);
    }

    /**
     * Delete step by id.
     *
     * @param id the id
     * @return the boolean
     */
    public boolean deleteStepById(UUID id) {
        int index = findStepIndex(id);
        if (index > -1) {
            this.steps.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Find step index int.
     *
     * @param id the id
     * @return the int
     */
    private int findStepIndex(UUID id) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Update step.
     *
     * @param stepEntity the step entity
     */
    public void updateStep(StepEntity stepEntity) {
        for (int i = 0; i < this.steps.size(); i++) {
            if (this.steps.get(i).equals(stepEntity)) {
                this.steps.set(i, stepEntity);
                break;
            }
        }
    }

    /**
     * Update queue.
     *
     * @param requestBody the request body
     */
    public void updateQueue(PrecisionQueueRequestBody requestBody) {
        this.setName(requestBody.getName());
        this.setMrd(requestBody.getMrd());
        this.setServiceLevelType(requestBody.getServiceLevelType());
        this.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
    }

    /**
     * Remove step boolean.
     *
     * @param stepEntity the step entity
     * @return the boolean
     */
    public boolean removeStep(StepEntity stepEntity) {
        return this.steps.remove(stepEntity);
    }

    @Override
    public String toString() {
        return "PrecisionQueue{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", mrd=" + mrd
                + ", serviceLevelType=" + serviceLevelType
                + ", serviceLevelThreshold=" + serviceLevelThreshold
                + ", steps=" + steps
                + '}';
    }
}
