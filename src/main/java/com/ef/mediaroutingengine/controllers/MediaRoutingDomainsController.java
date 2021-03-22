package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.services.controllerservices.MediaRoutingDomainsService;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MediaRoutingDomainsController {
    private final MediaRoutingDomainsService service;

    @Autowired
    public MediaRoutingDomainsController(MediaRoutingDomainsService service) {
        this.service = service;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/media-routing-domains", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Object> createMediaRoutingDomain(
            @Valid @RequestBody MediaRoutingDomain requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(value = "/media-routing-domains", produces = "application/json")
    public ResponseEntity<Object> retrieveMediaRoutingDomains() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping(value = "/media-routing-domains/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateMediaRoutingDomain(
            @Valid @RequestBody MediaRoutingDomain requestBody, @PathVariable UUID id) {
        return new ResponseEntity<>(this.service.update(requestBody, id), HttpStatus.OK);
    }

    /**
     * Delete a mrd.
     *
     * @param id UUID
     * @return ResponseEntity
     */
    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping(value = "/media-routing-domains/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteMediaRoutingDomain(@PathVariable UUID id) {
        List<PrecisionQueue> precisionQueues = this.service.delete(id);
        if (!precisionQueues.isEmpty()) {
            return new ResponseEntity<>(precisionQueues, HttpStatus.CONFLICT);
        }
        SuccessResponseBody responseBody = new SuccessResponseBody("Successfully Deleted");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
