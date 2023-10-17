package com.ef.mediaroutingengine.taskmanager.dto;

import com.ef.cim.objectmodel.task.TaskMediaState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Media state change req.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MediaStateChangeReq {
    /**
     * The State.
     */
    private TaskMediaState state;
}
