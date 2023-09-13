package com.ef.mediaroutingengine.routing.service;

import com.ef.mediaroutingengine.routing.utility.RestRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *  The KeyCloak Service.
 */
@Service
public class KeyCloakService {

    private final Logger logger = LoggerFactory.getLogger(KeyCloakService.class);

    private  final RestRequest restRequest;

    public KeyCloakService(RestRequest restRequest) {
        this.restRequest = restRequest;
    }

    /**
     *  Get token from keycloak.
     *
     * @return keycloak access token.
     */
    public  Object getToken() {

        Map<String, String> response = new HashMap<>();
        logger.info("Get KeyCloak token request initiated");
        String realm  = System.getenv("KEYCLOAK_REALM");
        String host  = System.getenv("KEYCLOAK_HOST");
        String url =  host + "realms/" + realm + "/protocol/openid-connect/token";
        logger.info("Request URL {} ",  url);
        MultiValueMap<String, String> body = this.getRequestBody();
        logger.debug("Request body {} ",  body);
        ResponseEntity<AccessTokenResponse> accessTokenResponse = this.restRequest.getToken(body, url);
        if (accessTokenResponse != null && accessTokenResponse.getBody() != null) {
            response.put("access_token", accessTokenResponse.getBody().getToken());
        }
        logger.info("Get KeyCloak token request handled gracefully");
        return  response;

    }



    private MultiValueMap<String, String> getRequestBody() {

        MultiValueMap<String, String> config = new LinkedMultiValueMap<>();
        config.add("client_id", System.getenv("KEYCLOAK_CLIENT_ID"));
        config.add("username", System.getenv("KEYCLOAK_USERNAME_ADMIN"));
        config.add("password", System.getenv("KEYCLOAK_PASSWORD_ADMIN"));
        config.add("grant_type", System.getenv("KEYCLOAK_GRANT_TYPE"));
        config.add("client_secret", System.getenv("KEYCLOAK_CLIENT_DB_ID"));
        config.add("scope", "openid");

        return config;

    }

}



