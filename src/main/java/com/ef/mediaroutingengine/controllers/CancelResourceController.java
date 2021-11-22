package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.CancelResourceRequest;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.services.controllerservices.CancelResourceService;
import java.util.concurrent.CompletableFuture;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type End task controller.
 */
@RestController
public class CancelResourceController {
    /**
     * The Service.
     */
    private final CancelResourceService service;

    /**
     * Instantiates a new End task controller.
     *
     * @param service the service
     */
    @Autowired
    public CancelResourceController(CancelResourceService service) {
        this.service = service;
    }

    /**
     * End task response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/cancel-resource")
    public ResponseEntity<Object> cancelResource(@Valid @RequestBody CancelResourceRequest request) {
        CompletableFuture.runAsync(() -> service.cancelResource(request));
        return ResponseEntity.accepted().body(new SuccessResponseBody("Request to cancel request "
                + "received successfully"));
    }
}
