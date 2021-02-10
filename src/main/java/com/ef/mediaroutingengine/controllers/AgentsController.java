package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.CCUser;
import com.ef.mediaroutingengine.services.controllerservices.AgentsService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.UUID;

@RestController
public class AgentsController {
    @Autowired
    private AgentsService service;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/agents", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createAgent(@Valid @RequestBody CCUser requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "/agents", produces = "application/json")
    public ResponseEntity<Object> retrieveAgents() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "/agents/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateAgent(@PathVariable UUID id, @Valid @RequestBody CCUser requestBody) {
        return new ResponseEntity<>(this.service.update(requestBody,id), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping(value = "/agents/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteAgent(@PathVariable UUID id) {
        this.service.delete(id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
