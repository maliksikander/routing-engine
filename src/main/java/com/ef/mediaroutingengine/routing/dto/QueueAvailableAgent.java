package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.global.utilities.ObjectMapperUtil;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
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
     * The id.
     */
    private String id;
    /**
     * The Name.
     */
    private String name;
    /**
     * The Ext.
     */
    private List<String> extensions;
    /**
     * The State.
     */
    private Enums.AgentMrdStateName state;

    /**
     * Instantiates a new Queue available agent.
     *
     * @param agent the agent
     * @param mrdId the mrd id
     */
    public QueueAvailableAgent(Agent agent, String mrdId) {
        this.id = agent.getId();
        this.name = agent.getKeycloakUser().displayName();
        this.extensions = getExtensions(agent.getKeycloakUser());
        this.state = agent.getAgentMrdState(mrdId).getState();
    }

    private List<String> getExtensions(KeycloakUser keycloakUser) {
        Object agentExtension = keycloakUser.getAttributes().get("agentExtension");

        if (agentExtension == null) {
            return new ArrayList<>();
        }

        return ObjectMapperUtil.get().convertValue(agentExtension, new TypeReference<>() {});
    }
}
