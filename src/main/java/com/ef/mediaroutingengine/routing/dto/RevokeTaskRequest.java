package com.ef.mediaroutingengine.routing.dto;


import com.ef.cim.objectmodel.task.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Revoke task request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RevokeTaskRequest {
    /**
     * The Task id.
     */
    private String taskId;
    /**
     * The Agent id.
     */
    private String agentId;
    /**
     * The Conversation id.
     */
    private String conversationId;

    /**
     * Instantiates a new Revoke task request.
     *
     * @param task the task
     */
    public RevokeTaskRequest(Task task) {
        this.taskId = task.getId();
        this.agentId = task.getAssignedTo().getId();
        this.conversationId = task.getConversationId();
    }
}
