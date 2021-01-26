package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

@RestController
public class PrecisionQueuesController {
    @PostMapping(value = "/v1/routing-engine/precision-queues", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createPrecisionQueue(@RequestBody PrecisionQueue precisionQueue) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            System.out.println(objectMapper.writeValueAsString(precisionQueue));
            return new ResponseEntity<>(objectMapper.writeValueAsString(precisionQueue), HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value = "/v1/routing-engine/precision-queues", produces = "application/json")
    public ResponseEntity<String> retrievePrecisionQueues() {
        JSONObject mrd = new JSONObject();
        mrd.put("id", "");
        mrd.put("name", "vddfd");
        mrd.put("description", "dfs");
        mrd.put("interruptible", true);

        JSONObject term = new JSONObject();
        term.put("attributeName", "");
        term.put("conditionOperator", "");
        term.put("value", false);

        JSONArray terms = new JSONArray();
        terms.put(term);

        JSONObject expression = new JSONObject();
        expression.put("terms", terms);

        JSONArray expressions = new JSONArray();
        expressions.put(expression);

        JSONObject step = new JSONObject();
        step.put("id", "");
        step.put("expressions", expressions);
        step.put("timeout", 43);

        JSONArray steps = new JSONArray();
        steps.put(step);

        JSONObject responseObject = new JSONObject();
        responseObject.put("id", "");
        responseObject.put("name", "Test");
        responseObject.put("mrd", mrd);
        responseObject.put("agentSelectionCriteria", "least skilled");
        responseObject.put("serviceLevelType", "1");
        responseObject.put("serviceLevelThreshold", 123);
        responseObject.put("steps", steps);

        JSONArray responseBody = new JSONArray();
        responseBody.put(responseObject);

        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @PutMapping(value = "/v1/routing-engine/precision-queues/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> updatePrecisionQueue(@RequestBody PrecisionQueue precisionQueue, @PathVariable String id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JSONObject responseBody = new JSONObject();
        responseBody.put("msg", "Successfully updated");
        responseBody.put("timestamp", timestamp.toString());
        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }

    @DeleteMapping(value = "/v1/routing-engine/precision-queues/{id}", produces = "application/json")
    public ResponseEntity<String> deletePrecisionQueue(@PathVariable String id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JSONObject responseBody = new JSONObject();
        responseBody.put("msg", "Successfully deleted");
        responseBody.put("timestamp", timestamp.toString());
        return new ResponseEntity<>(responseBody.toString(), HttpStatus.OK);
    }
}
