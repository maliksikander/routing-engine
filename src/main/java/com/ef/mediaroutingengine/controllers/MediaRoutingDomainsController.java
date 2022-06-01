package com.ef.mediaroutingengine.controllers;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.services.controllerservices.MediaRoutingDomainsService;
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

/**
 * Rest-Controller for the Media-routing-domains CRUD APIs.
 */
@RestController
public class MediaRoutingDomainsController {
    /**
     * The API calls are passed to this service for processing.
     */
    private final MediaRoutingDomainsService service;

    /**
     * Default Constructor. Loads the required dependency beans.
     *
     * @param service handles the actual processing for the API calls.
     */
    @Autowired
    public MediaRoutingDomainsController(MediaRoutingDomainsService service) {
        this.service = service;
    }

    /**
     * Create-MRD API handler. Creates a new MRD and sends back the API response.
     *
     * @param requestBody a MediaRoutingDomain object
     * @return the newly created MRD as the response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/media-routing-domains", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Object> createMediaRoutingDomain(
            @Valid @RequestBody MediaRoutingDomain requestBody) {
        return new ResponseEntity<>(this.service.create(requestBody), HttpStatus.OK);
    }

    /**
     * Retrieve-MRDs API handler. Returns the list of all MRDs in the config DB.
     *
     * @return list of all MRDs as response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/media-routing-domains", produces = "application/json")
    public ResponseEntity<Object> retrieveMediaRoutingDomains() {
        return new ResponseEntity<>(this.service.retrieve(), HttpStatus.OK);
    }

    /**
     * Update-MRD API handler. Updates an Existing MRD. Returns 404 Not-Found if the requested MRD
     * is not found.
     *
     * @param requestBody updated values of the MRD.
     * @param id          id of the MRD to update.
     * @return the updated MRD as response-body with status-code 200.
     */
    @CrossOrigin(origins = "*")
    @PutMapping(value = "/media-routing-domains/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateMediaRoutingDomain(
            @Valid @RequestBody MediaRoutingDomain requestBody, @PathVariable String id) {
        return this.service.update(requestBody, id);
    }

    /**
     * Delete-MRD API handler. Deletes an existing MRD. Returns 404 if the requested MRD is not found.
     * Returns HttpStatus-Conflict without deleting the MRD if the MRD is associated to any precision-queue.
     * In this case the list of precision-queues to which the MRD is associated is returned as response-body.
     *
     * @param id UUID of the MRD to be deleted
     * @return 200 on successful deletion, 404 if MRD is not found, 409 if MRD is associated with any queue.
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/media-routing-domains/{id}", produces = "application/json")
    public ResponseEntity<Object> deleteMediaRoutingDomain(@PathVariable String id) {
        return this.service.delete(id);
    }
}
