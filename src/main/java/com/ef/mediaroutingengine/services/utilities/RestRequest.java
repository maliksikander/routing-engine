package com.ef.mediaroutingengine.services.utilities;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.config.AssignResourceProperties;
import com.ef.mediaroutingengine.dto.AgentReservedRequest;
import com.ef.mediaroutingengine.dto.AssignTaskRequest;
import com.ef.mediaroutingengine.dto.ChangeStateRequest;
import java.time.Duration;
import java.util.UUID;
import org.json.JSONObject;
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
     * Calls the bot-framework's AgentReserved API.
     *
     * @param topicId id of the JMS topic.
     * @param agent   The agent that has been reserved on this topic.
     * @return The HTTP response from the API call.
     */
    public ResponseEntity<String> postAgentReserved(UUID topicId, CCUser agent) {
        AgentReservedRequest request = new AgentReservedRequest();
        request.setAgent(agent);
        request.setTopicId(topicId);

        return httpRequest(request, this.config.getAgentReservedUri(), HttpMethod.POST);
    }

    /**
     * Calls the bot-framework's No-Agent-Available API.
     *
     * @param topic id of the topic.
     * @return The HTTP response from the API call.
     */
    public ResponseEntity<String> postNoAgentAvailable(String topic) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("topicId", topic);
        return this.httpRequest(requestBody, config.getNoAgentAvailableUri(), HttpMethod.POST);
    }

    /**
     * Calls the Agent-manager's Assign-Task API.
     *
     * @param channelSession the channel-Session
     * @param agent          the agent to assign task to
     * @param topicId        the id of the JMS topic
     * @param taskId         the id of the task
     * @return true if request successful, false otherwise.
     */
    public boolean postAssignTask(ChannelSession channelSession, CCUser agent,
                                  UUID topicId, UUID taskId) {
        AssignTaskRequest request = new AssignTaskRequest();
        request.setChannelSession(channelSession);
        request.setCcUser(agent);
        request.setTopicId(topicId);
        request.setTaskId(taskId);

        try {
            this.httpRequest(request, this.config.getAssignTaskUri(), HttpMethod.POST);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
            resourceAccessException.printStackTrace();
        }

        return null;
    }
}
