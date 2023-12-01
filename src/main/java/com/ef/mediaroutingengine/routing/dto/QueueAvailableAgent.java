package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.routing.model.Agent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Queue available agent.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QueueAvailableAgent {
    /**
     * The State.
     */
    private Enums.AgentMrdStateName state;
    /**
     * The Agent.
     */
    private KeycloakUser agent;

    /**
     * Instantiates a new Queue available agent.
     *
     * @param agent the agent
     * @param mrdId the mrd id
     */
    public QueueAvailableAgent(Agent agent, String mrdId) {
        this.state = agent.getAgentMrdState(mrdId).getState();
        this.agent = agent.getKeycloakUser();
    }
}
