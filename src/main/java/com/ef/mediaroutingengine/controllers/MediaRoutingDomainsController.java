package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.controllerservices.MediaRoutingDomainsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
public class MediaRoutingDomainsController {
    @Autowired
    MediaRoutingDomainsService service;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/media-routing-domains", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> createMediaRoutingDomain(@Valid @RequestBody MediaRoutingDomain requestBody) {
        MediaRoutingDomain responseBody = service.create(requestBody);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @GetMapping(value = "/media-routing-domains", produces = "application/json")
    public ResponseEntity<Object> retrieveMediaRoutingDomains() {
        List<MediaRoutingDomain> responseBody = this.service.retrieve();
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PutMapping(value = "/media-routing-domains/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateMediaRoutingDomain(@Valid @RequestBody MediaRoutingDomain requestBody, @PathVariable UUID id) {
        this.service.update(requestBody, id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully updated");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @DeleteMapping(value = "/media-routing-domains/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteMediaRoutingDomain(@PathVariable UUID id) {
        this.service.delete(id);
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
