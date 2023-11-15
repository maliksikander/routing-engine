package com.ef.mediaroutingengine.routing.dto;


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
     * The Room id.
     */
    private String roomId;
}
