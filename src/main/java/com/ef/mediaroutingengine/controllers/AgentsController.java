package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
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

@RestController
public class AgentsController {
    private final AgentsService service;

    @Autowired
    public AgentsController(AgentsService service) {
        this.service = service;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/agents", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createAgent(@Valid @RequestBody CCUser requestBody) throws Exception {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "/agents", produces = "application/json")
    public ResponseEntity<Object> retrieveAgents() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "/agents/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateAgent(@PathVariable UUID id,
                                              @Valid @RequestBody CCUser requestBody) throws Exception {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    /**
     * Deletes a agent.
     *
     * @param id UUID
     * @return ResponseEntity
     */
    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping(value = "/agents/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteAgent(@PathVariable UUID id) throws Exception {
        this.service.delete(id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
