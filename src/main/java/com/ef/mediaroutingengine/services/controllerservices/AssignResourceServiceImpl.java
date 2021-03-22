package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.ChannelSession;
import com.ef.mediaroutingengine.config.AssignResourceProperties;
import com.ef.mediaroutingengine.dto.AgentReservedRequest;
import com.ef.mediaroutingengine.dto.AssignResourceRequest;
import com.ef.mediaroutingengine.dto.AssignTaskRequest;
import com.ef.mediaroutingengine.dto.ChangeStateRequest;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.RoutingEngineCache;
import com.ef.mediaroutingengine.services.AgentStateManager;
import com.ef.mediaroutingengine.services.AgentStateManagerImpl;
import com.ef.mediaroutingengine.services.FindAgent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignResourceServiceImpl implements AssignResourceService {

    private static final Logger log = LoggerFactory.getLogger(AssignResourceServiceImpl.class);
    private final AssignResourceProperties config;
    private final FindAgent findAgent;
    private final AgentStateManager agentStateManager;
    private final RoutingEngineCache routingEngineCache;

    /**
     * Default constructor.
     *
     * @param config Assign Resource properties
     * @param cache routing engine's cache
     * @param findAgent FindAgent object
     */
    @Autowired
    public AssignResourceServiceImpl(AssignResourceProperties config, RoutingEngineCache cache,
                                     FindAgent findAgent) {
        this.config = config;
        this.findAgent = findAgent;
        this.agentStateManager = new AgentStateManagerImpl();
        this.routingEngineCache = cache;
    }

    /**
     * Assigns a resource to a conversation.
     *
     * @param request AssignResourceRequest object
     */
    public void assign(AssignResourceRequest request) {
        log.debug("assign method started");
        for (int i = 0; i < this.config.getRetries(); i++) {
            log.debug("Assign Resource attempt no: {}", (i + 1));

            //Make task and add to cache.
            Task task = new Task();
            task.setTopicId(request.getTopicId());
            task.setState("CREATED");

            routingEngineCache.addTask(request.getTopicId(), task);
            log.debug("New Task Created and stored in cache");

            //Publish MOCK EWT
            ResponseEntity<String> agentEwtResponse = this.postAgentEwt(request.getTopicId(), 5);
            if (agentEwtResponse != null) {
                log.debug("Successfully published AGENT_EWT event");
            }

            // Find An Agent;
            CCUser agent = this.findAgent.find(true);
            //The find agent implementation will publish EWT event {not implemented in this mock implementation}
            //If agent found it will return an agent (CcUser object)
            //If agent not found, it will return null

            //If agent not Found
            if (agent == null) {
                log.debug("Agent not found");
                ResponseEntity<String> response = postNoAgentAvailable(request.getTopicId());

                if (response != null) {
                    log.debug("Successfully published NO_AGENT_AVAILABLE event");
                }

                routingEngineCache.removeTask(request.getTopicId());
                log.debug("Task removed from the cache");
                log.debug("assign method ended");
                return;
            }

            //If agent found change state
            boolean stateChanged = this.agentStateChanged(agent);

            if (stateChanged) {
                log.debug("Agent state changed to RESERVED");
                routingEngineCache.changeTaskState(request.getTopicId(), "RESERVED");
                log.debug("Task state changed to RESERVED in cache");

                //Publish AGENT_RESERVED event
                ResponseEntity<String> responseEntity = this
                        .postAgentReserved(request.getTopicId(), agent);
                if (responseEntity != null) {
                    log.debug("Successfully published AGENT_RESERVED event");
                }

                //Assign Task request to Agent Manager
                responseEntity = this.postAssignTask(request.getTopicId(), request.getChannelSession(), agent);
                if (responseEntity != null) {
                    log.debug("Assign Task request successful");
                }

                log.debug("assign method ended");
                return;
            } else {
                log.debug("State could not change trying reserving agent again...");
            }

            if (i == this.config.getRetries() - 1) {
                //publish Agent-Not-Available
                log.debug("Find Agent retries finished Publish No-Agent-Available");
            }
        }
        log.debug("assign method ended");
    }

    @Override
    public void assignResource(AssignResourceRequest request) {
        CCUser agent = this.findAgent.find(true);

        ResponseEntity<String> responseEntity = this.postAgentReserved(request.getTopicId(), agent);
        System.out.println("Assign Resource Topic: " + request.getTopicId());
        if (responseEntity != null) {
            log.debug("Successfully published AGENT_RESERVED event");
        }

        ResponseEntity<String> changeStateRes = this.postChangeState(agent, "READY");
        if (changeStateRes == null) {
            log.error("Change state request unsuccessful");
            return;
        }

        System.out.println("STATE HAS CHANGED");

        ResponseEntity<String> assignTaskRes = this.postAssignTask(request.getTopicId(),
                request.getChannelSession(), agent);

        if (assignTaskRes == null) {
            log.error("Assign Task request unsuccessful");
        }
    }

    private boolean agentStateChanged(CCUser agent) {
        ChangeStateRequest changeStateRequest = new ChangeStateRequest();
        changeStateRequest.setCcUser(agent);
        changeStateRequest.setState("RESERVED");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JSONObject requestBody = new JSONObject(
                    objectMapper.writeValueAsString(changeStateRequest));
            ResponseEntity<String> responseEntity = this
                    .httpRequest(requestBody, config.getChangeStateUri(), HttpMethod.PUT);

            if (responseEntity == null) {
                return false;
            }
            if (responseEntity.getStatusCodeValue() == 200) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private ResponseEntity<String> postAgentReserved(String topicId, CCUser agent) {
        AgentReservedRequest request = new AgentReservedRequest();
        request.setAgent(agent);
        request.setTopicId(topicId);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONObject requestBody = new JSONObject(objectMapper.writeValueAsString(request));
            System.out.println("POST AGENT RESERVED BODY: " + requestBody);
            return httpRequest(requestBody, this.config.getAgentReservedUri(), HttpMethod.POST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private ResponseEntity<String> postNoAgentAvailable(String topic) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("topicId", topic);
        return this.httpRequest(requestBody, config.getNoAgentAvailableUri(), HttpMethod.POST);
    }

    private ResponseEntity<String> postAgentEwt(String topicId, int ewt) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("topicId", topicId);
        requestBody.put("ewt", ewt);
        return this.httpRequest(requestBody, config.getAgentEwtUri(), HttpMethod.POST);
    }

    private ResponseEntity<String> postAssignTask(String topicId, ChannelSession channelSession,
                                                  CCUser agent) {
        AssignTaskRequest request = new AssignTaskRequest();
        request.setTopicId(topicId);
        request.setChannelSession(channelSession);
        request.setCcUser(agent);
        System.out.println("Channel session: " + channelSession.toString());
        System.out.println(144);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONObject requestBody = new JSONObject(objectMapper.writeValueAsString(request));
            System.out.println("POST ASSIGN TASK REQUEST BODY: " + requestBody);
            return httpRequest(requestBody, this.config.getAssignTaskUri(), HttpMethod.POST);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private ResponseEntity<String> postChangeState(CCUser ccUser, String state) {
        ChangeStateRequest request = new ChangeStateRequest();
        request.setCcUser(ccUser);
        request.setState(state);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONObject requestBody = new JSONObject(objectMapper.writeValueAsString(request));
            return httpRequest(requestBody, this.config.getChangeStateUri(), HttpMethod.PUT);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;

    }

    private ResponseEntity<String> httpRequest(JSONObject requestBody, String uri,
                                               HttpMethod httpMethod) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpRequest = new HttpEntity<>(requestBody.toString(), headers);

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
            System.out.println(resourceAccessException.getMessage());
        }

        return null;
    }
}
