package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.TestingStringLength;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestingController {
    @PostMapping(value = "/testing123", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> testingValidation(@RequestBody @Valid TestingStringLength requestBody) {
//        if(requestBody.getName()==null){
//            throw new IllegalArgumentException("Name is null");
//        }
        System.out.println(requestBody.toString());
        return new ResponseEntity<>(requestBody, HttpStatus.OK);
    }
}
