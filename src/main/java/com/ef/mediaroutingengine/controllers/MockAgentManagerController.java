package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.AssignTaskRequest;
import com.ef.mediaroutingengine.dto.ChangeStateRequest;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockAgentManagerController {
    @PutMapping(value = "/mockAgentManager/state", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> changeState(@RequestBody ChangeStateRequest request) {
        if(request.getState() == null || request.getCcUser() == null){
            return new ResponseEntity<>(invalidDataResponseBody(), HttpStatus.PRECONDITION_FAILED);
        }

        if(request.getCcUser().getDisplayName().equals("AGENT VINOD")){
            return new ResponseEntity<>(agentUnavailableResponseBody(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(successResponseBody(), HttpStatus.OK);
    }

    @PostMapping(value = "/mockAgentManager/agent/task", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> assignTask(@RequestBody AssignTaskRequest request){
        JSONObject responseBody = new JSONObject();
        responseBody.put("task", "assignTask");
        responseBody.put("msg", "ok");
        responseBody.put("statusCode", 202);

        return new ResponseEntity<>(responseBody.toString(), HttpStatus.ACCEPTED);
    }

    private String successResponseBody(){
        JSONObject responseBody = new JSONObject();
        responseBody.put("task", "changeAgentState");
        responseBody.put("msg", "state updated successfully");
        responseBody.put("statusCode", 200);

        return responseBody.toString();
    }

    private String invalidDataResponseBody(){
        JSONObject responseBody = new JSONObject();
        responseBody.put("task", "changeAgentState");
        responseBody.put("msg", "Precondition failed");
        responseBody.put("statusCode", 412);

        return responseBody.toString();
    }

    private String agentUnavailableResponseBody(){
        JSONObject responseBody = new JSONObject();
        responseBody.put("task", "changeAgentState");
        responseBody.put("msg", "Unprocessable entity");
        responseBody.put("statusCode", 422);

        return responseBody.toString();
    }

    private String customErrorResponseBody(){
        JSONObject responseBody = new JSONObject();
        responseBody.put("task", "changeAgentState");
        responseBody.put("msg", "some custom error");
        responseBody.put("statusCode", 500);

        return responseBody.toString();
    }
}
