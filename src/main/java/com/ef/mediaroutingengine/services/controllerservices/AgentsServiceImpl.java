package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AssociatedMrd;
import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
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
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The type Agents service.
 */
@Service
public class AgentsServiceImpl implements AgentsService {
    private final Logger logger = LoggerFactory.getLogger(AgentsServiceImpl.class);
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
        logger.info("Request to add CCUser initiated | CCUser: {}", ccUser.getKeycloakUser().getId());

        ccUser.setId(ccUser.getKeycloakUser().getId());
        this.validateAndSetRoutingAttributes(ccUser);
        logger.debug("CCUser's RoutingAttributes validated | CCUser: {}", ccUser.getId());

        if (this.repository.existsById(ccUser.getId())) {
            logger.debug("CCUser: {} exists, Updating existing CCUser", ccUser.getId());
            return this.update(ccUser);
        }

        Agent agent = new Agent(ccUser, mrdPool.findAll());
        logger.debug("Agent object created with associated MRDs | Agent: {}", agent.getId());

        //update the Associated MRDs & their maxTask values here in the ccUserObject
        this.setAssociatedMrdsAndMaxAgentTasks(ccUser, agent.getAgentMrdStates());

        AgentPresence agentPresence = new AgentPresence(ccUser, agent.getState(), agent.getAgentMrdStates());
        this.agentPresenceRepository.save(agent.getId().toString(), agentPresence);
        logger.debug("Agent inserted in Agent Presence Repository | Agent: {}", agent.getId());

        this.precisionQueuesPool.evaluateOnInsertForAll(agent);
        logger.debug("Agent's association in Queues evaluated | Agent: {}", agent.getId());

        this.agentsPool.insert(agent);
        logger.debug("Agent inserted in in-memory Agents pool | Agent: {}", agent.getId());

        CCUser insertedInDb = this.repository.insert(ccUser);
        logger.debug("Agent inserted in Agents config DB | Agent: {}", agent.getId());

        logger.info("CCUser added successfully | CCUser: {}", ccUser.getId());
        return insertedInDb;
    }

    @Override
    public List<CCUser> retrieve() {
        return this.repository.findAll();
    }

    @Override
    public CCUser update(CCUser ccUser, UUID id) {
        logger.info("Request to update CCUser initiated | CCUser: {}", id);

        if (!this.repository.existsById(id)) {
            String errorMessage = "Could not find CCUser resource to update | CCUserId: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        ccUser.setId(id);
        this.validateAndSetRoutingAttributes(ccUser);
        logger.debug("CCUser's RoutingAttributes validated | CCUser: {}", ccUser.getId());

        return this.update(ccUser);
    }

    private CCUser update(CCUser ccUser) {
        Agent agent = this.agentsPool.findById(ccUser.getId());
        agent.updateFrom(ccUser);
        logger.debug("Agent updated in in-memory Agents pool | Agent: {}", agent.getId());

        this.agentPresenceRepository.updateCcUser(ccUser);
        logger.debug("Agent updated in Agent Presence Repository | Agent: {}", agent.getId());

        this.precisionQueuesPool.evaluateOnUpdateForAll(agent);
        logger.debug("Agent's association in Queues re-evaluated | Agent: {}", agent.getId());

        CCUser savedInDb = this.repository.save(ccUser);
        logger.debug("Agent updated in Agents config DB | Agent: {}", agent.getId());

        logger.info("CCUser updated successfully | CCUser: {}", ccUser.getId());
        return savedInDb;
    }

    @Override
    public ResponseEntity<Object> delete(UUID id) {
        logger.info("Request to remove routing-attributes from CCUser initiated | CCUser: {}", id);

        Optional<CCUser> optionalCcUser = this.repository.findById(id);

        if (optionalCcUser.isEmpty()) {
            String errorMessage = "Could not find CCUser resource to delete | CCUserId: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        Agent agent = this.agentsPool.findById(id);

        List<Task> tasks = agent.getAllTasks();
        if (!tasks.isEmpty()) {
            logger.error("Could not delete agent, there are tasks associated to the agent | Agent: {}", id);
            List<TaskDto> taskDtoList = new ArrayList<>();
            tasks.forEach(task -> taskDtoList.add(new TaskDto(task)));
            return new ResponseEntity<>(taskDtoList, HttpStatus.CONFLICT);
        }

        CCUser ccUser = optionalCcUser.get();
        ccUser.setAssociatedRoutingAttributes(new ArrayList<>());
        ccUser.setAssociatedMrds(new ArrayList<>());

        agent.updateFrom(ccUser);
        logger.debug("All routing-attributes removed from Agent in in-memory pool | Agent: {}", id);

        this.agentPresenceRepository.updateCcUser(ccUser);
        logger.debug("All routing-attributes removed from Agent in Agent-Presence Repository | Agent: {}", id);

        this.precisionQueuesPool.deleteFromAll(agent);
        logger.debug("Agent's Association removed from all Queues | Agent: {}", id);

        this.repository.save(ccUser);
        logger.debug("All routing-attributes removed from Agent in Agents-Config-DB | Agent: {}", id);

        logger.info("Routing-attributes removed successfully from CCUser | CCUser: {}", id);
        return new ResponseEntity<>(new SuccessResponseBody("Successfully deleted"), HttpStatus.OK);
    }

    /**
     * Validate and set routing attributes.
     *
     * @param ccUser the cc user
     */
    void validateAndSetRoutingAttributes(CCUser ccUser) {
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
                logger.error("CCUser's RoutingAttributes validation failed | CCUser: {}", ccUser.getId());
                throw new NotFoundException("Could not find routing-attribute resource");
            }
            associatedRoutingAttribute.setRoutingAttribute(routingAttribute);
        }
    }

    /**
     * Validate and set Associated MRDs and their max tasks.
     *
     * @param ccUser the cc user
     */
    void setAssociatedMrdsAndMaxAgentTasks(CCUser ccUser, List<AgentMrdState> agentMrdStates) {
        if (agentMrdStates == null) {
            logger.error("Could not find agent MRD states. {} ", ccUser.getId());
            return;
        }
        agentMrdStates.forEach(
                mrd -> ccUser.addAssociatedMrd(new AssociatedMrd(mrd.getMrd().getId(), mrd.getMaxAgentTask())));
    }

    void saveUpdatedAgentInDb(CCUser agent) {
        repository.save(agent);
        logger.debug("Agent updated in Agents config DB | Agent: {}", agent.getId());
    }
}
