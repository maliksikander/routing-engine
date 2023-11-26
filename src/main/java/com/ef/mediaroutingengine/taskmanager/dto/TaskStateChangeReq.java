package com.ef.mediaroutingengine.taskmanager.dto;

import com.ef.cim.objectmodel.task.TaskState;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Task state change req.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskStateChangeReq {
    /**
     * The Conversation id.
     */
    @NotBlank
    String conversationId;
    /**
     * The State.
     */
    @NotNull
    TaskState state;
}
