package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The type Agents service.
 */
@Service
public class AgentsServiceImpl implements AgentsService {

    /**
     * The Repository.
     */
    private final AgentsRepository repository;
    /**
     * The Routing attributes pool.
     */
    private final RoutingAttributesPool routingAttributesPool;
    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;
    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;
    /**
     * The Precision queues pool.
     */
    private final PrecisionQueuesPool precisionQueuesPool;
    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;

    /**
     * Constructor. Autowired, loads the beans.
     *
     * @param repository              to communicate with Agents collection in the DB.
     * @param routingAttributesPool   the routing attributes pool
     * @param agentsPool              the agents pool
     * @param mrdPool                 the mrd pool
     * @param precisionQueuesPool     the precision queues pool
     * @param agentPresenceRepository the agent presence repository
     */
    @Autowired
    public AgentsServiceImpl(AgentsRepository repository,
                             RoutingAttributesPool routingAttributesPool, AgentsPool agentsPool,
                             MrdPool mrdPool, PrecisionQueuesPool precisionQueuesPool,
                             AgentPresenceRepository agentPresenceRepository) {
        this.repository = repository;
        this.routingAttributesPool = routingAttributesPool;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentPresenceRepository = agentPresenceRepository;
    }

    @Override
    public CCUser create(CCUser ccUser) {
        ccUser.setId(ccUser.getKeycloakUser().getId());
        this.validateAndSetRoutingAttributes(ccUser);

        Agent agent = new Agent(ccUser, mrdPool.findAll());
        AgentPresence agentPresence = new AgentPresence(ccUser, agent.getState(), agent.getAgentMrdStates());

        this.agentPresenceRepository.save(agent.getId().toString(), agentPresence);
        this.precisionQueuesPool.evaluateOnInsertForAll(agent);
        this.agentsPool.insert(agent);
        return this.repository.insert(ccUser);
    }

    @Override
    public List<CCUser> retrieve() {
        return this.repository.findAll();
    }

    @Override
    public CCUser update(CCUser ccUser, UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find agent resource to update");
        }
        ccUser.setId(id);
        this.validateAndSetRoutingAttributes(ccUser);
        Agent agent = this.agentsPool.findById(id);
        agent.updateFrom(ccUser);
        this.agentPresenceRepository.updateCcUser(ccUser);
        this.precisionQueuesPool.evaluateOnUpdateForAll(agent);
        return this.repository.save(ccUser);
    }

    @Override
    public ResponseEntity<Object> delete(UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find agent resource to delete");
        }
        Agent agent = this.agentsPool.findById(id);
        List<Task> tasks = agent.getAllTasks();
        if (!tasks.isEmpty()) {
            List<TaskDto> taskDtoList = new ArrayList<>();
            tasks.forEach(task -> taskDtoList.add(new TaskDto(task)));
            return new ResponseEntity<>(taskDtoList, HttpStatus.CONFLICT);
        }
        this.agentsPool.deleteById(id);
        this.agentPresenceRepository.deleteById(id.toString());
        this.precisionQueuesPool.deleteFromALl(agent);
        this.repository.deleteById(id);
        return new ResponseEntity<>(new SuccessResponseBody("Successfully deleted"), HttpStatus.OK);
    }

    /**
     * Validate and set routing attributes.
     *
     * @param ccUser the cc user
     */
    private void validateAndSetRoutingAttributes(CCUser ccUser) {
        List<AssociatedRoutingAttribute> associatedRoutingAttributes = ccUser.getAssociatedRoutingAttributes();
        if (associatedRoutingAttributes == null) {
            return;
        }
        for (AssociatedRoutingAttribute associatedRoutingAttribute : associatedRoutingAttributes) {
            RoutingAttribute requestedRoutingAttribute = associatedRoutingAttribute.getRoutingAttribute();
            if (requestedRoutingAttribute == null) {
                continue;
            }
            RoutingAttribute routingAttribute = this.routingAttributesPool.findById(requestedRoutingAttribute.getId());
            if (routingAttribute == null) {
                throw new NotFoundException("Could not find routing-attribute resource");
            }
            associatedRoutingAttribute.setRoutingAttribute(routingAttribute);
        }
    }
}
