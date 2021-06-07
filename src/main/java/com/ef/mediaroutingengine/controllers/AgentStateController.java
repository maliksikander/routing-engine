package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.AgentLoginRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.eventlisteners.AgentStateListener;
import com.ef.mediaroutingengine.model.Enums;
import java.beans.PropertyChangeSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentStateController {
    private final PropertyChangeSupport propertyChangeSupport;

    /**
     * Constructor. Loads the required beans.
     *
     * @param agentStateListener handles the changes in agents' state
     */
    @Autowired
    public AgentStateController(AgentStateListener agentStateListener) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.propertyChangeSupport.addPropertyChangeListener(Enums.EventName.AGENT_STATE.name(),
                agentStateListener);
    }

    @PostMapping("/agent-state")
    public ResponseEntity<Object> agentState(@RequestBody AgentStateChangeRequest request) {
        this.propertyChangeSupport.firePropertyChange(Enums.EventName.AGENT_STATE.name(), null, request);
        return new ResponseEntity<>(new SuccessResponseBody("Agent state change request received"), HttpStatus.OK);
    }

    /**
     * Handles the Agents Login request from Agent Manager.
     *
     * @param request AgentLoginRequest Dto
     * @return ResponseEntity
     */
    @PostMapping("/agent-login")
    public ResponseEntity<Object> agentLogin(@RequestBody AgentLoginRequest request) {
        AgentStateChangeRequest agentStateChangeRequest = new AgentStateChangeRequest();
        agentStateChangeRequest.setAgentId(request.getAgentId());
        agentStateChangeRequest.setState(Enums.AgentStateName.LOGIN);
        this.propertyChangeSupport.firePropertyChange(Enums.EventName.AGENT_STATE.name(), null,
                agentStateChangeRequest);
        return new ResponseEntity<>(new SuccessResponseBody("Agent login request received"), HttpStatus.OK);
    }
}
