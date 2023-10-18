package com.ef.mediaroutingengine.routing.controller;

import com.ef.mediaroutingengine.routing.service.MrdTypesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mrd types controller.
 */
@RestController
public class MrdTypesController {
    /**
     * The Service.
     */
    private final MrdTypesService service;

    /**
     * Instantiates a new Mrd types controller.
     *
     * @param service the service
     */
    @Autowired
    public MrdTypesController(MrdTypesService service) {
        this.service = service;
    }

    /**
     * Gets mrd types.
     *
     * @return the mrd types
     */
    @GetMapping("/mrd-types")
    public ResponseEntity<Object> getMrdTypes() {
        return ResponseEntity.ok().body(this.service.getMrdTypes());
    }
}
