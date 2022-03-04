package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.services.controllerservices.AgentStateService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest-Controller for the Agent login, Agent state and Agent-MRD state APIs.
 */
@RestController
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateController {
    /**
     * The API calls are passed to this service for processing.
     */
    private final AgentStateService agentStateService;

    /**
     * Default Constructor. Loads the required beans.
     *
     * @param agentStateService handles the actual processing for the API calls.
     */
    @Autowired
    public AgentStateController(AgentStateService agentStateService) {
        this.agentStateService = agentStateService;
    }

    /**
     * Handles the Agent-Login request from Agent Manager.
     *
     * @param request AgentLoginRequest Dto
     * @return ResponseEntity response entity
     */
    @PostMapping("/agent-login")
    public ResponseEntity<Object> agentLogin(@Valid @RequestBody KeycloakUser request) {
        this.agentStateService.agentLogin(request);
        return new ResponseEntity<>(new SuccessResponseBody("Agent login request received"), HttpStatus.OK);
    }

    /**
     * Handles the Agent-State change request from Agent Manager.
     *
     * @param request AgentStateChangeRequest Dto
     * @return ResponseEntity response entity
     */
    @PutMapping("/agent-state")
    public ResponseEntity<Object> agentState(@Valid @RequestBody AgentStateChangeRequest request) {
        this.agentStateService.agentState(request);
        return new ResponseEntity<>(new SuccessResponseBody("Agent state change request received"), HttpStatus.OK);
    }

    /**
     * Handles the Agent-MRD-State change request from Agent Manager.
     *
     * @param request AgentMrdStateChangeRequest DTO
     * @return Success response if request received successfully.
     */
    @PutMapping("/mrd-state")
    public ResponseEntity<Object> mrdState(@Valid @RequestBody AgentMrdStateChangeRequest request) {
        this.agentStateService.agentMrdState(request);
        return new ResponseEntity<>(new SuccessResponseBody("Agent MRD state change request received"),
                HttpStatus.OK);
    }
}
