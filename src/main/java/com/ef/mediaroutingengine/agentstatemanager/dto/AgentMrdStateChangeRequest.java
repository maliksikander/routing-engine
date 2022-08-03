package com.ef.mediaroutingengine.agentstatemanager.dto;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.agentstatemanager.controller.AgentStateController;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An AgentMrdStateChangeRequest object is used by the
 * {@link AgentStateController#mrdState(AgentMrdStateChangeRequest)
 * MRD-State-Change}*** API as Request Body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AgentMrdStateChangeRequest {
    /**
     * ID of the agent, for which the MRD-state-change is requested.
     */
    @NotNull
    String agentId;
    /**
     * Agent's Associated MRD for which the state change is requested.
     */
    @NotNull
    String mrdId;
    /**
     * The New Agent-MRD-State requested.
     */
    @NotNull
    Enums.AgentMrdStateName state;
}
