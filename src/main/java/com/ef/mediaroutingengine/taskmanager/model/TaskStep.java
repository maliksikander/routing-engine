package com.ef.mediaroutingengine.taskmanager.model;

import com.ef.mediaroutingengine.routing.model.Step;

/**
 * The type Task step.
 */
public class TaskStep {
    /**
     * The Step.
     */
    private Step step;
    /**
     * The Is last step.
     */
    private boolean isLastStep;

    /**
     * Instantiates a new Task step.
     *
     * @param step       the step
     * @param isLastStep the is last step
     */
    public TaskStep(Step step, boolean isLastStep) {
        this.step = step;
        this.isLastStep = isLastStep;
    }

    /**
     * Gets step.
     *
     * @return the step
     */
    public Step getStep() {
        return step;
    }

    /**
     * Sets step.
     *
     * @param step the step
     */
    public void setStep(Step step) {
        this.step = step;
    }

    /**
     * Is last step boolean.
     *
     * @return the boolean
     */
    public boolean isLastStep() {
        return isLastStep;
    }

    /**
     * Sets last step.
     *
     * @param lastStep the last step
     */
    public void setLastStep(boolean lastStep) {
        isLastStep = lastStep;
    }
}
