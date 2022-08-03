package com.ef.mediaroutingengine.agentstatemanager.dto;

import com.ef.cim.objectmodel.AgentState;
import com.ef.mediaroutingengine.agentstatemanager.controller.AgentStateController;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * An AgentStateChangeRequest object is used by the
 * {@link AgentStateController#agentState(AgentStateChangeRequest)
 * Agent-State-Change}*** API as Request Body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AgentStateChangeRequest {
    /**
     * ID of the agent for which the Agent-State change is requested.
     */
    @NotNull
    String agentId;
    /**
     * The new Agent-State requested.
     */
    @NotNull
    @Valid
    AgentState state;
}
