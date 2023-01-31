package com.ef.mediaroutingengine.routing.utility;

import com.ef.mediaroutingengine.config.AssignResourceProperties;
import com.ef.mediaroutingengine.global.commons.Constants;
import java.time.Duration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * The type Rest request.
 */
@Service
public class RestRequest {
    /**
     * The constant LOGGER.
     */
    private final Logger logger = LoggerFactory.getLogger(RestRequest.class);
    /**
     * The Config.
     */
    private final AssignResourceProperties config;

    /**
     * Instantiates a new Rest request.
     *
     * @param config the config
     */
    @Autowired
    public RestRequest(AssignResourceProperties config) {
        this.config = config;
    }


    /**
     * Makes an httpRequest.
     *
     * @param requestBody The body of the request.
     * @param uri         the uri of the request.
     * @param httpMethod  the http-method e.g. GET, POST, PUT
     * @return The HTTP response from the request.
     */
    public ResponseEntity<String> httpRequest(Object requestBody, String uri,
                                              HttpMethod httpMethod) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Constants.MDC_CORRELATION_ID, MDC.get(Constants.MDC_CORRELATION_ID));

        HttpEntity<Object> httpRequest = new HttpEntity<>(requestBody, headers);

        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        Duration duration = Duration.ofSeconds(5);
        RestTemplate restTemplate = restTemplateBuilder.setConnectTimeout(duration).build();

        try {
            switch (httpMethod) {
                case POST:
                    return restTemplate.postForEntity(uri, httpRequest, String.class);
                case PUT:
                    return restTemplate.exchange(uri, HttpMethod.PUT, httpRequest, String.class);
                default:
                    String error = "Only POST, PUT allowed";
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

        } catch (ResourceAccessException resourceAccessException) {
            logger.error(ExceptionUtils.getMessage(resourceAccessException));
            logger.error(ExceptionUtils.getStackTrace(resourceAccessException));
        }

        return null;
    }
}
