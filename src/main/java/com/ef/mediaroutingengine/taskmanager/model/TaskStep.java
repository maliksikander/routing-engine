package com.ef.mediaroutingengine.taskmanager.model;

import com.ef.mediaroutingengine.routing.model.Step;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Task step.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskStep {
    /**
     * The Step.
     */
    private Step step;
    /**
     * The Is last step.
     */
    private boolean isLastStep;
}
