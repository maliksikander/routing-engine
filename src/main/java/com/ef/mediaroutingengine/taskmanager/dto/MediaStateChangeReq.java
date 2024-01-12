package com.ef.mediaroutingengine.taskmanager.dto;

import com.ef.cim.objectmodel.task.TaskMediaState;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
     * The Conversation id.
     */
    @NotBlank
    private String conversationId;
    /**
     * The State.
     */
    @NotNull
    private TaskMediaState state;
}
