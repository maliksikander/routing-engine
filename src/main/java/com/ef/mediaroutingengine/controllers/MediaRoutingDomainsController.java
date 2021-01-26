package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.UUID;

@RestController
public class MediaRoutingDomainsController {
    @PostMapping(value = "/v1/routing-engine/media-routing-domains", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createMediaRoutingDomain(@RequestBody MediaRoutingDomain mediaRoutingDomain) {
        JSONObject responseBody = new JSONObject();
        responseBody.put("name", mediaRoutingDomain.getName());
        responseBody.put("description", mediaRoutingDomain.getDescription());
        responseBody.put("interruptible", mediaRoutingDomain.isInterruptible());

        mediaRoutingDomain.setId(UUID.randomUUID());
        responseBody.put("_id", mediaRoutingDomain.getId());
        responseBody.put("__v", 0);

        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/v1/routing-engine/media-routing-domains", produces = "application/json")
    public ResponseEntity<String> retrieveMediaRoutingDomains() {
        JSONArray responseBody = new JSONArray();

        JSONObject responseObject = new JSONObject();
        responseObject.put("name", "Test");
        responseObject.put("description", "Test");
        responseObject.put("interruptible", true);

        responseObject.put("_id", UUID.randomUUID().toString());
        responseObject.put("__v", 0);

        responseBody.put(responseObject);

        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @PutMapping(value = "/v1/routing-engine/media-routing-domains/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> updateMediaRoutingDomain(@RequestBody MediaRoutingDomain mediaRoutingDomain, @PathVariable String id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JSONObject responseBody = new JSONObject();
        responseBody.put("msg", "Successfully updated");
        responseBody.put("timestamp", timestamp.toString());
        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @DeleteMapping(value = "/v1/routing-engine/media-routing-domains/{id}", produces = "application/json")
    public ResponseEntity<String> deleteMediaRoutingDomain(@PathVariable String id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JSONObject responseBody = new JSONObject();
        responseBody.put("msg", "Successfully deleted");
        responseBody.put("timestamp", timestamp.toString());
        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

}
