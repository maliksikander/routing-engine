package com.ef.mediaroutingengine.routing.utility;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.config.AssignResourceProperties;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.dto.AssignTaskRequest;
import com.ef.mediaroutingengine.routing.dto.RevokeTaskRequest;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
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
     * Calls the Agent-manager's Assign-Task API.
     *
     * @param task  the task dto
     * @param agent the agent to assign task to
     * @return true if request successful, false otherwise.
     */
    public boolean postAssignTask(Task task, CCUser agent, TaskState taskState, boolean async) {
        if (async) {
            String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
            CompletableFuture.runAsync(() -> {
                MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
                MDC.put(Constants.MDC_TOPIC_ID, task.getTopicId());
                this.postAssignTask(task, agent, taskState);
                MDC.clear();
            });
            return true;
        }

        return postAssignTask(task, agent, taskState);
    }

    private boolean postAssignTask(Task task, CCUser agent, TaskState taskState) {
        TaskDto taskDto = AdapterUtility.createTaskDtoFrom(task);
        taskDto.setState(taskState);
        AssignTaskRequest request = new AssignTaskRequest(taskDto, agent);

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
     * Calls the agent manager's Revoke task API.
     *
     * @param task the task.
     * @return true if request is successful
     */
    public boolean postRevokeTask(Task task) {
        RevokeTaskRequest requestBody = new RevokeTaskRequest(task.getId(), task.getAssignedTo(), task.getTopicId());
        try {
            this.httpRequest(requestBody, this.config.getRevokeTaskUri(), HttpMethod.POST);
            return true;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
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
