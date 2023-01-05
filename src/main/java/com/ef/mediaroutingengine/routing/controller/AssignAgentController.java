package com.ef.mediaroutingengine.routing.controller;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.dto.AssignAgentRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.service.AssignAgentService;
import java.util.concurrent.CompletableFuture;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Assign agent controller.
 */
@RestController
public class AssignAgentController {
    private static final Logger logger = LoggerFactory.getLogger(AssignAgentController.class);

    /**
     * The Service.
     */
    private final AssignAgentService service;
    private final MrdPool mrdPool;
    private final AgentsPool agentsPool;

    /**
     * Instantiates a new Assign agent controller.
     *
     * @param service    the service
     * @param mrdPool    the mrd pool
     * @param agentsPool the agents pool
     */
    @Autowired
    public AssignAgentController(AssignAgentService service, MrdPool mrdPool,
                                 AgentsPool agentsPool) {
        this.service = service;
        this.mrdPool = mrdPool;
        this.agentsPool = agentsPool;
    }

    /**
     * Assign agent response entity.
     *
     * @param req the request body
     * @return the response entity
     */
    @CrossOrigin("*")
    @PostMapping("/assign-agent")
    public ResponseEntity<Object> assignAgent(@Valid @RequestBody AssignAgentRequest req,
                                              @RequestParam(required = false) boolean updateTask,
                                              @RequestParam(required = false) boolean offerToAgent) {

        Agent agent = this.validateAndGetAgent(req.getAgent());

        String mrdId = req.getChannelSession().getChannel().getChannelType().getMediaRoutingDomain();
        MediaRoutingDomain mrd = this.validateAndGetMrd(mrdId);

        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);

        CompletableFuture.runAsync(() -> {
            MDC.put(Constants.MDC_CORRELATION_ID, correlationId);

            this.service.assign(req, agent, mrd, updateTask, offerToAgent);

            MDC.clear();
        });


        return ResponseEntity.accepted().body(new SuccessResponseBody("Assign Agent request received"));
    }

    private Agent validateAndGetAgent(String agentId) {
        Agent agent = this.agentsPool.findById(agentId);

        if (agent == null) {
            String errMessage = "No Agent found with id: " + agentId;
            logger.error(errMessage);
            throw new NotFoundException(errMessage);
        }

        return agent;
    }

    private MediaRoutingDomain validateAndGetMrd(String mrdId) {
        MediaRoutingDomain mrd = this.mrdPool.findById(mrdId);

        if (mrd == null) {
            String errMessage = "No MRD found with id: " + mrdId;
            logger.error(errMessage);
            throw new NotFoundException(errMessage);
        }

        return mrd;
    }
}
