package com.ef.mediaroutingengine.routing.utility;

import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.routing.dto.AgentAssociated;
import com.ef.mediaroutingengine.routing.model.Agent;
import io.micrometer.core.instrument.util.StringUtils;

/**
 * Type Agent util.
 */
public class AgentUtil {

    /**
     * Method to get AgentDetail from Agent.
     *
     * @param agent the agent object
     * @param queueId the queue id
     * @return the return object
     */
    public static AgentAssociated getAgentDetailFromAgent(Agent agent, String queueId) {
        return new AgentAssociated(
                agent.getState(), agent.getKeycloakUser(),
                agent.getAgentTasksCountByQueueId(queueId), agent.getAgentMrdStates());
    }


    /**
     * to get the agent name.
     *
     * @param keycloakUser keycloak object
     * @return agent name
     */
    public static String getAgentName(KeycloakUser keycloakUser) {

        if (!StringUtils.isBlank(keycloakUser.getFirstName()) && !StringUtils.isBlank(keycloakUser.getLastName())) {
            return keycloakUser.getFirstName() + " " + keycloakUser.getLastName();
        } else if (!StringUtils.isBlank(keycloakUser.getFirstName())) {
            return keycloakUser.getFirstName();
        } else if (!StringUtils.isBlank(keycloakUser.getLastName())) {
            return keycloakUser.getLastName();
        }

        return keycloakUser.getUsername();
    }

}
