package com.ef.mediaroutingengine.routing.controller;


import com.ef.mediaroutingengine.routing.service.KeyCloakService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest-Controller for the Keycloak token.
 */

@RestController
public class KeyCloakController {

    private  final KeyCloakService keyCloakService;

    public KeyCloakController(KeyCloakService keyCloakService) {
        this.keyCloakService = keyCloakService;
    }

    /**
     * Return the keycloak token.
     *
     * @return token 200.
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/keycloak_token", produces = "application/json")
    public ResponseEntity<Object> getToken() {
        return new ResponseEntity<>(this.keyCloakService.getToken(), HttpStatus.OK);
    }


}
