package com.ef.mediaroutingengine.routing.utility;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.dto.QueueHistoricalStatsDto;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskMediaState;
import com.ef.mediaroutingengine.config.ExternalServiceConfig;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.utilities.ObjectToUrlEncodedConverter;
import com.ef.mediaroutingengine.routing.dto.AssignTaskRequest;
import com.ef.mediaroutingengine.routing.dto.RevokeTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.keycloak.representations.AccessTokenResponse;
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
import org.springframework.util.MultiValueMap;
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
    private final ExternalServiceConfig config;
    /**
     * The Rest Template.
     */
    private final RestTemplate restTemplate;

    /**
     * Instantiates a new Rest request.
     *
     * @param config the config
     */
    @Autowired
    public RestRequest(ExternalServiceConfig config) {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        Duration duration = Duration.ofSeconds(5);
        this.restTemplate = restTemplateBuilder.setConnectTimeout(duration).build();
        this.config = config;
    }

    /**
     * Post assign task boolean.
     *
     * @param task  the task
     * @param media the media
     * @param state the state
     * @param agent the agent
     * @param async the async
     * @return the boolean
     */
    public boolean postAssignTask(Task task, TaskMedia media, TaskMediaState state, CCUser agent, boolean async) {
        if (async) {
            String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
            CompletableFuture.runAsync(() -> {
                MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
                MDC.put(Constants.MDC_TOPIC_ID, task.getConversationId());
                this.postAssignTask(task, media, state, agent);
                MDC.clear();
            });
            return true;
        }

        return postAssignTask(task, media, state, agent);
    }

    /**
     * Post assign task boolean.
     *
     * @param task  the task
     * @param media the media
     * @param state the state
     * @param agent the agent
     * @return the boolean
     */
    private boolean postAssignTask(Task task, TaskMedia media, TaskMediaState state, CCUser agent) {
        media.setState(state);
        AssignTaskRequest request = new AssignTaskRequest(task.getId(), task.getConversationId(), media, agent);

        try {
            this.httpRequest(request, this.config.getAssignTaskUri(), HttpMethod.POST);
            return true;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /**
     * Post revoke task.
     *
     * @param task  the task
     * @param async the async
     */
    public void postRevokeTask(Task task, boolean async) {
        if (!async) {
            this.postRevokeTask(task);
            return;
        }

        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
        CompletableFuture.runAsync(() -> {
            MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
            MDC.put(Constants.MDC_TOPIC_ID, task.getConversationId());
            this.postRevokeTask(task);
            MDC.clear();
        });

    }

    /**
     * Post revoke task.
     *
     * @param task the task
     */
    private void postRevokeTask(Task task) {
        try {
            this.httpRequest(new RevokeTaskRequest(task), this.config.getRevokeTaskUri(), HttpMethod.POST);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
        }
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

    /**
     * Get request to fetch Historical stats from Real-Time-Reports.
     *
     * @param queueId the queue for which the stats are required.
     * @return returns the QueueHistoricalStats DTO.
     */
    public QueueHistoricalStatsDto getQueueHistoricalStats(String queueId) {
        String url = config.getRealTimeReportsUri() + "/queue/" + queueId + "/historical-stats";
        return this.restTemplate.getForEntity(url, QueueHistoricalStatsDto.class).getBody();
    }

    /**
     * Makes Post request to keycloak.
     *
     * @param map Request Body.
     * @param uri Request URL.
     * @return AccessTokenResponse Object.
     */
    public ResponseEntity<AccessTokenResponse> getToken(MultiValueMap<String, String> map, String uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestBodyFormUrlEncoded = new HttpEntity<>(map, headers);

        restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(new ObjectMapper()));

        try {
            return restTemplate.postForEntity(uri, requestBodyFormUrlEncoded, AccessTokenResponse.class);
        } catch (ResourceAccessException resourceAccessException) {
            logger.error(ExceptionUtils.getMessage(resourceAccessException));
            logger.error(ExceptionUtils.getStackTrace(resourceAccessException));
        }

        return null;
    }
}
