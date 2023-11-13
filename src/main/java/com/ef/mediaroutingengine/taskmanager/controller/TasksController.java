package com.ef.mediaroutingengine.taskmanager.controller;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.task.TaskState;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.taskmanager.dto.MediaStateChangeReq;
import com.ef.mediaroutingengine.taskmanager.service.TasksService;
import com.ef.mediaroutingengine.taskmanager.service.taskmediastate.TaskMediaStateService;
import com.ef.mediaroutingengine.taskmanager.service.taskstate.TaskStateListener;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.validation.Valid;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Get Tasks Rest Controller.
 */
@RestController
@RequestMapping("tasks")
public class TasksController {
    /**
     * The Service.
     */
    private final TasksService service;
    /**
     * The Task state listener.
     */
    private final TaskStateListener taskStateListener;
    /**
     * The Task media state service.
     */
    private final TaskMediaStateService taskMediaStateService;

    /**
     * Instantiates a new Tasks controller.
     *
     * @param service               the service
     * @param taskStateListener     the task state listener
     * @param taskMediaStateService the task media state service
     */
    @Autowired
    public TasksController(TasksService service, TaskStateListener taskStateListener,
                           TaskMediaStateService taskMediaStateService) {
        this.service = service;
        this.taskStateListener = taskStateListener;
        this.taskMediaStateService = taskMediaStateService;
    }

    /**
     * Retrieve by id response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/{id}")
    public ResponseEntity<Object> retrieveById(@PathVariable String id) {
        return new ResponseEntity<>(this.service.retrieveById(id), HttpStatus.OK);
    }

    /**
     * Retrieve response entity.
     *
     * @param agentId the agent id
     * @return the response entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping("")
    public ResponseEntity<Object> retrieve(@RequestParam Optional<String> agentId) {
        return new ResponseEntity<>(this.service.retrieveAll(agentId), HttpStatus.OK);
    }

    /**
     * Change task state response entity.
     *
     * @param taskId  the task id
     * @param request the request
     * @return the response entity
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/{taskId}/change-state")
    public ResponseEntity<Object> changeTaskState(@PathVariable String taskId,
                                                  @Valid @RequestBody TaskState request) {
        return ResponseEntity.ok().body(taskStateListener.propertyChange(taskId, request));
    }

    /**
     * Change task media state response entity.
     *
     * @param taskId  the task id
     * @param mediaId the media id
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/{taskId}/{mediaId}/change-state")
    public ResponseEntity<Object> changeTaskMediaState(@PathVariable String taskId,
                                                       @PathVariable String mediaId,
                                                       @Valid @RequestBody MediaStateChangeReq request) {
        return ResponseEntity.ok().body(this.taskMediaStateService.changeState(taskId, mediaId, request.getState()));
    }

    /**
     * The endpoint retrieves the ewt and position of a task.
     *
     * @param conversationId The conversation for which the request is made.
     * @return The ewt and position.
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/{conversationId}/ewt")
    public ResponseEntity<Object> getEwtAndPosition(@PathVariable String conversationId) {
        return this.service.getEwtAndPosition(conversationId);
    }

    /**
     * Add session response entity.
     *
     * @param channelSession the channel session
     * @return the response entity
     */
    @PostMapping("/medias/sessions")
    public ResponseEntity<Object> addSession(@RequestBody ChannelSession channelSession) {
        this.service.addSession(channelSession);
        return ResponseEntity.ok().body(null);
    }

    /**
     * Remove session response entity.
     *
     * @param channelSession the channel session
     * @return the response entity
     */
    @DeleteMapping("/medias/sessions")
    public ResponseEntity<Object> removeSession(@RequestBody ChannelSession channelSession) {
        this.service.removeSession(channelSession);
        return ResponseEntity.ok().body(null);
    }

    /**
     * Cancel resource by direction response entity.
     *
     * @param conversationId the conversation id
     * @param direction      the direction
     * @return the response entity
     */
    @DeleteMapping("/conversation/{conversationId}/direction/{direction}")
    public ResponseEntity<Object> revokeResourceByDirection(@PathVariable String conversationId,
                                                            @PathVariable Enums.TaskTypeDirection direction) {
        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
        CompletableFuture.runAsync(() -> {
            MDC.put(Constants.MDC_CORRELATION_ID, correlationId);
            MDC.put(Constants.MDC_TOPIC_ID, conversationId);
            this.service.revokeResourceByDirection(conversationId, direction);
            MDC.clear();
        });

        return ResponseEntity.accepted().body(new SuccessResponseBody("Revoke resource by direction request accepted"));
    }
}
