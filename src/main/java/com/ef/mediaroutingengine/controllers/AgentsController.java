package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.CCUser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

@RestController
public class AgentsController {
    @GetMapping(value = "/v1/routing-engine/agents", produces = "application/json")
    public ResponseEntity<String> retrieveAgents() {
        JSONArray roles = new JSONArray();
        JSONArray permittedResources = new JSONArray();

        JSONObject keyCloakUser = new JSONObject();
        keyCloakUser.put("id", "600016622b258400063fe52d");
        keyCloakUser.put("firstName", "Nikola");
        keyCloakUser.put("lastName", "Tesla");
        keyCloakUser.put("roles", roles);
        keyCloakUser.put("permittedResources", permittedResources);
        keyCloakUser.put("realm", "Agent-Manager");


        JSONObject routingAttribute = new JSONObject();
        routingAttribute.put("createdDate", "2021-01-14T10:01:06.399Z");
        routingAttribute.put("_id", "600016622b258400063fe52d");
        routingAttribute.put("name", "ee");
        routingAttribute.put("description", "ee-description");
        routingAttribute.put("type", "BOOLEAN");
        routingAttribute.put("value", "true");

        JSONArray routingAttributes = new JSONArray();
        routingAttributes.put(routingAttribute);

        JSONObject responseObject = new JSONObject();
        responseObject.put("keyCloakUser", keyCloakUser);
        responseObject.put("routingAttributes", routingAttributes);
        responseObject.put("__v", 0);

        JSONArray responseBody = new JSONArray();
        responseBody.put(responseObject);

        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @PutMapping(value = "/v1/routing-engine/agents/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> updateAgents(@RequestBody CCUser ccUser, @PathVariable String id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JSONObject responseBody = new JSONObject();
        responseBody.put("msg", "Successfully updated");
        responseBody.put("timestamp", timestamp.toString());
        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }
}
