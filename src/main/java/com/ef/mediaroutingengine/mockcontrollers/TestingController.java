package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.model.TestingStringLength;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestingController {
    @PostMapping(value = "/testing123", consumes = "application/json", produces = "application/json")
    public String testingValidation(@Valid @RequestBody TestingStringLength requestBody) {
        System.out.println(requestBody.toString());
        return "Success";
    }
}