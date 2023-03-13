package com.ef.mediaroutingengine.routing.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The response DTO which have the details of agents that are associated to a particular queue.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AssociatedAgentsResponse {
    /**
     * the queue id.
     */
    private String queueId;
    /**
     * The Queue name.
     */
    private String queueName;
    /**
     * The Agents.
     */
    private List<AssociatedAgentEntity> agents;
}
