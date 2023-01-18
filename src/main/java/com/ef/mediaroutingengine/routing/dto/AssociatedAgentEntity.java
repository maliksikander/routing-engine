package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.routing.model.Agent;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Associated to queue dto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AssociatedAgentEntity {
    /**
     * The State.
     */
    private AgentState state;
    /**
     * The Key cloak detail.
     */
    private KeycloakUser keyCloakDetail;
    /**
     * The Active tasks count.
     */
    private long activeTasksCount;
    /**
     * The Mrd states.
     */
    private List<AgentMrdState> mrdStates;

    /**
     * Instantiates a new Agent associated.
     *
     * @param agent   the agent
     * @param queueId the queue id
     */
    public AssociatedAgentEntity(Agent agent, String queueId) {
        this. state = agent.getState();
        this.keyCloakDetail = agent.getKeycloakUser();
        this.activeTasksCount = agent.getActiveTasksCountByQueueId(queueId);
        this.mrdStates = agent.getAgentMrdStates();
    }
}