package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.AgentLoginRequest;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.eventlisteners.agentmrdstate.AgentMrdStateListener;
import com.ef.mediaroutingengine.eventlisteners.agentstate.AgentStateListener;
import com.ef.mediaroutingengine.model.AgentState;
import java.beans.PropertyChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateService {
    private final AgentStateListener agentStateListener;
    private final AgentMrdStateListener agentMrdStateListener;

    @Autowired
    public AgentStateService(AgentStateListener agentStateListener, AgentMrdStateListener agentMrdStateListener) {
        this.agentStateListener = agentStateListener;
        this.agentMrdStateListener = agentMrdStateListener;
    }

    /**
     * Handles the agent login request.
     *
     * @param request AgentLoginRequest DTO.
     */
    public void agentLogin(AgentLoginRequest request) {
        AgentStateChangeRequest agentStateChangeRequest = new AgentStateChangeRequest(request.getAgentId(),
                new AgentState(Enums.AgentStateName.LOGIN, null));
        this.agentState(agentStateChangeRequest);
    }

    public void agentState(AgentStateChangeRequest request) {
        PropertyChangeEvent evt = getPropertyChangeEvent(Enums.EventName.AGENT_STATE, request);
        this.agentStateListener.propertyChange(evt);
    }

    public void agentMrdState(AgentMrdStateChangeRequest request) {
        PropertyChangeEvent evt = getPropertyChangeEvent(Enums.EventName.AGENT_MRD_STATE, request);
        this.agentMrdStateListener.propertyChange(evt, true);
    }

    private PropertyChangeEvent getPropertyChangeEvent(Enums.EventName event, Object value) {
        return new PropertyChangeEvent(this, event.name(), null, value);
    }
}