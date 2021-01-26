package com.ef.mediaroutingengine.controllers;

import com.ef.cim.objectmodel.RoutingAttribute;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.UUID;

@RestController
public class RoutingAttributesController {
    @PostMapping(value = "/api/v1/routing-engine/routing-attributes", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createRoutingAttribute(@RequestBody RoutingAttribute routingAttribute) {
        JSONObject responseBody = new JSONObject();
        responseBody.put("name", routingAttribute.getName());
        responseBody.put("description", routingAttribute.getDescription());
        responseBody.put("type", routingAttribute.getType());
        responseBody.put("value", routingAttribute.getDefaultValue().getValue());

        routingAttribute.setId(UUID.randomUUID());
        responseBody.put("_id", routingAttribute.getId());

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        responseBody.put("createdDate", timestamp.toString());
        responseBody.put("__v", 0);

        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/api/v1/routing-engine/routing-attributes", produces = "application/json")
    public ResponseEntity<String> retrieveRoutingAttributes() {
        JSONArray responseBody = new JSONArray();

        JSONObject responseObject = new JSONObject();
        responseObject.put("name", "Test");
        responseObject.put("description", "Test");
        responseObject.put("type", "BOOLEAN");
        responseObject.put("value", "anim nisi");
        responseObject.put("_id", "1");
        responseObject.put("createdDate", "6006b0c60861a60007de97f4");
        responseObject.put("__v", 0);
        
        responseBody.put(responseObject);

        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @PutMapping(value = "/api/v1/routing-engine/routing-attributes/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> updateRoutingAttribute(@RequestBody RoutingAttribute routingAttribute, @PathVariable String id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JSONObject responseBody = new JSONObject();
        responseBody.put("msg", "Successfully updated");
        responseBody.put("timestamp", timestamp.toString());
        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/v1/routing-engine/routing-attributes/{id}", produces = "application/json")
    public ResponseEntity<String> deleteRoutingAttribute(@PathVariable String id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JSONObject responseBody = new JSONObject();
        responseBody.put("msg", "Successfully deleted");
        responseBody.put("timestamp", timestamp.toString());
        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }
}
