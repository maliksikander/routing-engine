package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.KeycloakUser;
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
public class AgentAssociated {
    private AgentState state;
    private KeycloakUser keyCloakDetail;
    private long activeTasksCount;
    private List<AgentMrdState> mrdStates;
}