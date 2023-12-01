package com.ef.mediaroutingengine.routing.dto;

import com.ef.mediaroutingengine.routing.model.PrecisionQueue;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type is used for fetching queues with available agents.
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class QueuesWithAvailableAgentsRes {
    /**
     * The Queue id.
     */
    private String queueId;
    /**
     * The Queue name.
     */
    private String queueName;
    /**
     * The Mrd id.
     */
    private String mrdId;
    /**
     * The Total available agents.
     */
    private int totalAvailableAgents;
    /**
     * The Available agents.
     */
    private List<QueueAvailableAgent> availableAgents = new ArrayList<>();

    /**
     * Instantiates a new Queues with available agent response.
     *
     * @param queue  the queue
     * @param agents the agents
     */
    public QueuesWithAvailableAgentsRes(PrecisionQueue queue, List<QueueAvailableAgent> agents) {
        this.queueId = queue.getId();
        this.queueName = queue.getName();
        this.mrdId = queue.getMrd().getId();
        this.totalAvailableAgents = agents.size();
        this.availableAgents = agents;
    }
}
