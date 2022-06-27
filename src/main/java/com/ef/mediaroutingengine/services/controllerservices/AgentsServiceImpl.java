package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.AgentState;
import com.ef.cim.objectmodel.AssociatedMrd;
import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.commons.Constants;
import com.ef.mediaroutingengine.dto.AgentMrdStateChangeRequest;
import com.ef.mediaroutingengine.dto.AssociatedMrdUpdateConflictResponse;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.exceptions.ForbiddenException;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.PrecisionQueuesPool;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import com.ef.mediaroutingengine.services.utilities.AdapterUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * The Agent State service.
     */
    private final AgentStateService agentStateService;

    /**
     * Constructor. Autowired, loads the beans.
     *
     * @param repository              to communicate with Agents collection in the DB.
     * @param routingAttributesPool   the routing attributes pool
     * @param agentsPool              the agents pool
     * @param mrdPool                 the mrd pool
     * @param precisionQueuesPool     the precision queues pool
     * @param agentPresenceRepository the agent presence repository
     * @param agentStateService       the agent state service
     */
    @Autowired
    public AgentsServiceImpl(AgentsRepository repository,
                             RoutingAttributesPool routingAttributesPool, AgentsPool agentsPool,
                             MrdPool mrdPool, PrecisionQueuesPool precisionQueuesPool,
                             AgentPresenceRepository agentPresenceRepository,
                             AgentStateService agentStateService) {
        this.repository = repository;
        this.routingAttributesPool = routingAttributesPool;
        this.agentsPool = agentsPool;
        this.mrdPool = mrdPool;
        this.precisionQueuesPool = precisionQueuesPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.agentStateService = agentStateService;
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

        AgentPresence agentPresence = new AgentPresence(ccUser, agent.getState(), agent.getAgentMrdStates());
        this.agentPresenceRepository.save(agent.getId().toString(), agentPresence);
        logger.debug("Agent inserted in Agent Presence Repository | Agent: {}", agent.getId());

        //update the Associated MRDs & their maxTask values here in the ccUserObject
        this.setAssociatedMrdsAndMaxAgentTasks(ccUser, agent.getAgentMrdStates());

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
    public ResponseEntity<Object> update(CCUser ccUser, UUID id) {
        logger.info("Request to update CCUser initiated | CCUser: {}", id);

        if (!this.repository.existsById(id)) {
            String errorMessage = "Could not find CCUser resource to update | CCUserId: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        ccUser.setId(id);

        AssociatedMrdUpdateConflictResponse associatedMrdUpdateConflictResponse = getConflictedAssociatedMrds(ccUser);
        if (!associatedMrdUpdateConflictResponse.getAssociatedMrds().isEmpty()) {
            return new ResponseEntity<>(associatedMrdUpdateConflictResponse, HttpStatus.CONFLICT);
        }

        this.validateAndSetRoutingAttributes(ccUser);
        logger.debug("CCUser's RoutingAttributes validated | CCUser: {}", ccUser.getId());

        return new ResponseEntity<>(this.update(ccUser), HttpStatus.OK);
    }

    private CCUser update(CCUser ccUser) {
        Agent agent = this.agentsPool.findById(ccUser.getId());

        if (ccUser.getAssociatedMrds().isEmpty()) {
            ccUser.setAssociatedMrds(agent.toCcUser().getAssociatedMrds());
        }

        agent.updateFrom(ccUser);
        logger.debug("Agent updated in in-memory Agents pool | Agent: {}", agent.getId());

        this.agentPresenceRepository.updateCcUser(ccUser);
        logger.debug("Agent updated in Agent Presence Repository | Agent: {}", agent.getId());

        this.precisionQueuesPool.evaluateOnUpdateForAll(agent);
        logger.debug("Agent's association in Queues re-evaluated | Agent: {}", agent.getId());

        this.updateAgentMrdState(ccUser);

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
            tasks.forEach(task -> taskDtoList.add(AdapterUtility.createTaskDtoFrom(task)));
            return new ResponseEntity<>(taskDtoList, HttpStatus.CONFLICT);
        }

        CCUser ccUser = optionalCcUser.get();
        ccUser.setAssociatedRoutingAttributes(new ArrayList<>());

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
     * This method will update the Agent MRD State.
     */
    protected void updateAgentMrdState(CCUser ccUser) {
        UUID agentId = ccUser.getId();
        Agent agent = this.agentsPool.findById(agentId);
        AgentState agentState = agent.getState();

        ccUser.getAssociatedMrds().forEach(
                associatedMrd -> {
                    String mrdId = associatedMrd.getMrdId();
                    int maxAgentTasks = associatedMrd.getMaxAgentTasks();

                    int agentActivePushTasks = agent.getNoOfActivePushTasks(mrdId);
                    AgentMrdState agentMrdState = agent.getAgentMrdState(mrdId);

                    logger.debug(
                            "Agent-ID : {} --- MRD-ID : {} --- Active Push-Tasks = {} --- & maxAgentTasks = {} --- & "
                                    + " AgentMrdState = {} |",
                            agentId, mrdId,
                            agentActivePushTasks,
                            maxAgentTasks, agentMrdState);

                    if (agentMrdState.getState().equals(Enums.AgentMrdStateName.ACTIVE)
                            && (agentActivePushTasks >= maxAgentTasks || maxAgentTasks == 0)) {
                        putAgentMrdStateChangeRequest(agentId, mrdId, Enums.AgentMrdStateName.BUSY);
                        logger.debug("MRD state has been changed from ACTIVE to BUSY. |");
                    }

                    if (agentMrdState.getState().equals(Enums.AgentMrdStateName.BUSY)
                            && agentActivePushTasks < maxAgentTasks && maxAgentTasks != 0) {
                        putAgentMrdStateChangeRequest(agentId, mrdId, Enums.AgentMrdStateName.ACTIVE);
                        logger.debug("MRD state has been changed from BUSY to ACTIVE. |");
                    }

                    if (maxAgentTasks == 0 && agentState.getName().equals(Enums.AgentStateName.READY)
                            && agentMrdState.getState().equals(Enums.AgentMrdStateName.READY)) {
                        putAgentMrdStateChangeRequest(agentId, mrdId, Enums.AgentMrdStateName.NOT_READY);
                        logger.debug("MRD state has been changed from READY to NOT_READY.|");
                    }
                }
        );
    }

    /**
     * This method will prepare the AgentMrdStateChange Request and update the state.
     */
    private void putAgentMrdStateChangeRequest(UUID agentId, String mrdId, Enums.AgentMrdStateName agentMrdStateName) {
        AgentMrdStateChangeRequest request =
                new AgentMrdStateChangeRequest(agentId, mrdId, agentMrdStateName);
        agentStateService.agentMrdState(request);
    }

    /**
     * Validate and set Associated MRDs and their max tasks.
     *
     * @param ccUser the cc user
     */
    protected void setAssociatedMrdsAndMaxAgentTasks(CCUser ccUser, List<AgentMrdState> agentMrdStates) {
        if (agentMrdStates == null) {
            logger.error("Could not find agent MRD states against agent-id. {} ", ccUser.getId());
            return;
        }
        agentMrdStates.forEach(
                mrd -> ccUser.addAssociatedMrd(new AssociatedMrd(mrd.getMrd().getId(), mrd.getMaxAgentTasks())));
    }

    protected void saveUpdatedAgentInDb(CCUser agent) {
        repository.save(agent);
        logger.debug("Agent updated in Agents config DB | Agent: {}", agent.getId());
    }

    /**
     * This method will return a list of Associated MRDs.
     * who's maxAgentTasks are greater than the maxMrdRequest against an MRD.
     */
    protected AssociatedMrdUpdateConflictResponse getConflictedAssociatedMrds(CCUser ccUser) {
        List<AssociatedMrd> conflictedAssociatedMrds = new ArrayList<>();
        AtomicBoolean isInvalidMaxAgentTasks = new AtomicBoolean(false);
        Agent agent = this.agentsPool.findById(ccUser.getId());

        ccUser.getAssociatedMrds().forEach(
                associatedMrd -> {
                    int mrdMaxRequests = agent.getAgentMrdState(associatedMrd.getMrdId()) != null
                            ? agent.getAgentMrdState(associatedMrd.getMrdId()).getMrd().getMaxRequests() : 0;

                    if (associatedMrd.getMrdId().equals(Constants.VOICE_MRD_ID)
                            && associatedMrd.getMaxAgentTasks() != 1) {
                        String errorMessage = "The maxAgentTasks against VOICE MRD should be equal to 1.";
                        logger.error(errorMessage);
                        throw new ForbiddenException(errorMessage);
                    }

                    if (associatedMrd.getMaxAgentTasks() < 0) {
                        isInvalidMaxAgentTasks.set(true);
                        conflictedAssociatedMrds.add(associatedMrd);
                        conflictedAssociatedMrds.removeIf(mrd -> mrd.getMaxAgentTasks() >= 0);
                    }

                    if (associatedMrd.getMaxAgentTasks() > mrdMaxRequests && mrdMaxRequests != 0
                            && !isInvalidMaxAgentTasks.get()) {
                        conflictedAssociatedMrds.add(associatedMrd);
                    }
                }
        );

        String reason = getAssociatedMrdUpdateConflictReason(isInvalidMaxAgentTasks.get(), ccUser.getId());
        return new AssociatedMrdUpdateConflictResponse(ccUser.getId(), reason, conflictedAssociatedMrds);
    }

    /**
     * This method will return the Reason behind the conflict of Associated MRD Update of an Agent.
     */
    private String getAssociatedMrdUpdateConflictReason(boolean isInvalidMaxAgentTasks, UUID id) {
        String reason = "";
        if (isInvalidMaxAgentTasks) {
            reason = "Failed to update the Agent with ID : " + id
                    + ".Because the following Associated MRDs have agentMaxTasks less than 0"
                    + ".The agentMaxTasks value should be >= 0.";
        }

        if (!isInvalidMaxAgentTasks) {
            reason = "Failed to update the Agent with ID : " + id
                    + ".Because the following Associated MRDs have MaxTasks which are greater than"
                    + "the concerned MRD maxRequest value"
                    + ".The new agentMaxTasks value should be <= MRD MaxRequest against a particular MRD.";
        }
        return reason;
    }
}