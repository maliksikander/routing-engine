package com.ef.mediaroutingengine.routing.controller;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.dto.AssignAgentRequest;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.service.AssignAgentService;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final AgentsPool agentsPool;

    /**
     * Instantiates a new Assign agent controller.
     *
     * @param service    the service
     * @param agentsPool the agents pool
     */
    @Autowired
    public AssignAgentController(AssignAgentService service, AgentsPool agentsPool) {
        this.service = service;
        this.agentsPool = agentsPool;
    }

    /**
     * Assign agent response entity.
     *
     * @param req the request body
     * @return the response entity.
     */
    @CrossOrigin("*")
    @PostMapping("/assign-agent")
    public ResponseEntity<Object> assignAgent(@Valid @RequestBody AssignAgentRequest req) {
        logger.debug("call received to assign agent");

        if (!req.getType().getMode().equals(Enums.TaskTypeMode.AGENT)) {
            String errorMsg = "type.mode should be AGENT";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        Agent agent = this.agentsPool.findBy(req.getAgent());
        this.validate(agent, req.getAgent());

        return ResponseEntity.ok().body(this.service.assign(req, agent));
    }

    private void validate(Agent agent, String agentId) {
        if (agent == null) {
            String errMessage = "No Agent found with id: " + agentId;
            logger.error(errMessage);
            throw new NotFoundException(errMessage);
        }
    }
}
