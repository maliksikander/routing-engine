package com.ef.mediaroutingengine.agentstatemanager.controller;

import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateApiRes;
import com.ef.mediaroutingengine.agentstatemanager.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.agentstatemanager.service.AgentStateService;
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
    public ResponseEntity<Object> agentLogin(@RequestBody KeycloakUser request) {
        this.agentStateService.agentLogin(request);

        AgentStateApiRes resBody = new AgentStateApiRes(request.getId(), "Agent login request received");
        return new ResponseEntity<>(resBody, HttpStatus.OK);
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

        AgentStateApiRes resBody = new AgentStateApiRes(request.getAgentId(), "Agent state change request received");
        return new ResponseEntity<>(resBody, HttpStatus.OK);
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

        String resMessage = "Agent MRD state change request received";
        AgentStateApiRes resBody = new AgentStateApiRes(request.getAgentId(), resMessage);
        return new ResponseEntity<>(resBody, HttpStatus.OK);
    }
}
