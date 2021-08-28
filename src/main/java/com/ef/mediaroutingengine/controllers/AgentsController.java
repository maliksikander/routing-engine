package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.services.controllerservices.AgentsService;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest-Controller for the Agents CRUD APIs.
 */
@RestController
public class AgentsController {
    /**
     * The API calls are passed to this service for processing.
     */
    private final AgentsService service;

    /**
     * Default Constructor. Loads the required dependency beans.
     *
     * @param service handles the actual processing for the API calls.
     */
    @Autowired
    public AgentsController(AgentsService service) {
        this.service = service;
    }

    /**
     * Create-Agent API handler. Creates a new Agent and sends back the API response.
     *
     * @param requestBody a CcUser object
     * @return the newly created agent as the response-body with status-code 200.
     * @throws Exception if there is an issue. Proper error response is sent back by the ResponseExceptionHandler.
     */
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/agents", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createAgent(@Valid @RequestBody CCUser requestBody) throws Exception {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    /**
     * Retrieve-Agents API handler. Returns the list of all agents in the config DB.
     *
     * @return list of all Agents as response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/agents", produces = "application/json")
    public ResponseEntity<Object> retrieveAgents() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    /**
     * Updates-Agent API handler. Updates an Existing agent. Returns 404 Not-Found if the requested agent
     * is not found.
     *
     * @param id          id of the agent to update.
     * @param requestBody updated value of the agent.
     * @return the updated agent as response-body with status-code 200.
     * @throws Exception In case an agent is not found or depended data like routing-attributes is inconsistent.
     */
    @CrossOrigin(origins = "*")
    @PutMapping(value = "/agents/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateAgent(@PathVariable UUID id,
                                              @Valid @RequestBody CCUser requestBody) throws Exception {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    /**
     * Delete-Agent API handler. Deletes an existing agent. Returns 404 if the requested agent is not found.
     *
     * @param id UUID of the agent to be deleted
     * @return Success message response with status code 200
     * @throws Exception the exception
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/agents/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteAgent(@PathVariable UUID id) throws Exception {
        return this.service.delete(id);
    }
}
