package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.AgentLoginRequest;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AgentStateChangeRequest;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.services.controllerservices.AgentStateService;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateController {
    private final AgentStateService agentStateService;

    @Autowired
    public AgentStateController(AgentStateService agentStateService) {
        this.agentStateService = agentStateService;
    }

    /**
     * Handles the Agents Login request from Agent Manager.
     *
     * @param request AgentLoginRequest Dto
     * @return ResponseEntity
     */
    @PostMapping("/agent-login")
    public ResponseEntity<Object> agentLogin(@RequestBody AgentLoginRequest request) {
        CompletableFuture.runAsync(() -> this.agentStateService.agentLogin(request));
        return new ResponseEntity<>(new SuccessResponseBody("Agent login request received"), HttpStatus.OK);
    }

    @PutMapping("/agent-state")
    public ResponseEntity<Object> agentState(@RequestBody AgentStateChangeRequest request) {
        this.agentStateService.agentState(request);
        return new ResponseEntity<>(new SuccessResponseBody("Agent state change request received"), HttpStatus.OK);
    }

    /**
     * API to change the Agent MRD state.
     *
     * @param request AgentMrdStateChangeRequest DTO
     * @return Success response if request received successfully.
     */
    @PutMapping("/mrd-state")
    public ResponseEntity<Object> mrdState(@RequestBody AgentMrdStateChangeRequest request) {
        this.agentStateService.agentMrdState(request);
        return new ResponseEntity<>(new SuccessResponseBody("Agent MRD state change request received"),
                HttpStatus.OK);
    }
}
