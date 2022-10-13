package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.KeycloakUser;
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
public class QueuesWithAvailableAgentsResponse {
    private String queueId;
    private String queueName;
    private int totalAvailableAgents;
    private List<KeycloakUser> availableAgents = new ArrayList<>();
}
