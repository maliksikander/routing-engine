package com.ef.mediaroutingengine.agentstatemanager.dto;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Agent state api res.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AgentStateApiRes {
    /**
     * The Agent id.
     */
    private String agentId;
    /**
     * The Timestamp.
     */
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    /**
     * The Message.
     */
    private String message;

    /**
     * Instantiates a new Agent state api res.
     *
     * @param agentId the agent id
     * @param message the message
     */
    public AgentStateApiRes(String agentId, String message) {
        this.agentId = agentId;
        this.message = message;
    }
}
