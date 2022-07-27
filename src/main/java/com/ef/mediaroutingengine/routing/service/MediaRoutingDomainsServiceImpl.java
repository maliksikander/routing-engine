package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.AgentMrdState;
import com.ef.cim.objectmodel.AgentPresence;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.agentstatemanager.repository.AgentPresenceRepository;
import com.ef.mediaroutingengine.global.commons.Constants;
import com.ef.mediaroutingengine.global.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.global.exceptions.ForbiddenException;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.global.utilities.AdapterUtility;
import com.ef.mediaroutingengine.routing.dto.MrdDeleteConflictResponse;
import com.ef.mediaroutingengine.routing.dto.MrdUpdateConflictResponse;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.pool.AgentsPool;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import com.ef.mediaroutingengine.routing.repository.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.taskmanager.model.Task;
import com.ef.mediaroutingengine.taskmanager.pool.TasksPool;
import com.ef.mediaroutingengine.taskmanager.repository.TasksRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Media routing domains service.
 */
@Service
public class MediaRoutingDomainsServiceImpl implements MediaRoutingDomainsService {
    /**
     * The constant LOGGER.
     */
    private static final Logger logger = LoggerFactory.getLogger(MediaRoutingDomainsServiceImpl.class);
    /**
     * The Repository.
     */
    private final MediaRoutingDomainRepository repository;
    /**
     * The Precision queue entity repository.
     */
    private final PrecisionQueueRepository precisionQueueRepository;

    /**
     * The Tasks pool.
     */
    private final TasksPool tasksPool;

    /**
     * The Mrd pool.
     */
    private final MrdPool mrdPool;

    /**
     * The Agents pool.
     */
    private final AgentsPool agentsPool;

    /**
     * The Agent presence repository.
     */
    private final AgentPresenceRepository agentPresenceRepository;

    /**
     * The Tasks repository.
     */
    private final TasksRepository tasksRepository;

    /**
     * The Agent Service Impl.
     */
    private final AgentsServiceImpl agentsService;

    /**
     * Constructor, Autowired, loads the beans.
     *
     * @param repository               to communicate with MRD collection in DB
     * @param precisionQueueRepository to communicate with PrecisionQueues collection in DB
     * @param tasksPool                the tasks pool
     * @param mrdPool                  the mrd pool
     * @param agentsPool               the agents pool
     * @param agentPresenceRepository  the agent presence repository
     * @param tasksRepository          the tasks repository
     */
    @Autowired
    public MediaRoutingDomainsServiceImpl(MediaRoutingDomainRepository repository,
                                          PrecisionQueueRepository precisionQueueRepository,
                                          TasksPool tasksPool, MrdPool mrdPool, AgentsPool agentsPool,
                                          AgentPresenceRepository agentPresenceRepository,
                                          TasksRepository tasksRepository,
                                          AgentsServiceImpl agentsService) {
        this.repository = repository;
        this.precisionQueueRepository = precisionQueueRepository;
        this.tasksPool = tasksPool;
        this.mrdPool = mrdPool;
        this.agentsPool = agentsPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.tasksRepository = tasksRepository;
        this.agentsService = agentsService;
    }

    @Override
    public MediaRoutingDomain create(MediaRoutingDomain mediaRoutingDomain) {
        logger.info("Create MRD request initiated");
        MediaRoutingDomain inserted = repository.insert(mediaRoutingDomain);
        logger.debug("MRD inserted in MRD Config DB | MRD: {}", inserted.getId());

        this.mrdPool.insert(inserted);
        logger.debug("MRD inserted in in-memory MRD pool | MRD: {}", inserted.getId());

        AgentMrdState agentMrdState = new AgentMrdState(inserted, Enums.AgentMrdStateName.NOT_READY);
        for (Agent agent : agentsPool.findAll()) {
            agent.addAgentMrdState(agentMrdState);
        }
        logger.debug("MRD associated to all Agents in in-memory Agents pool | MRD: {}", inserted.getId());

        Map<String, AgentPresence> agentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : this.agentPresenceRepository.findAll()) {
            agentPresence.getAgentMrdStates().add(agentMrdState);
            agentPresenceMap.put(agentPresence.getAgent().getId().toString(), agentPresence);
        }
        this.agentPresenceRepository.saveAllByKeyValueMap(agentPresenceMap);
        logger.debug("MRD associated to all Agents in Agent presence Repository | MRD: {}", inserted.getId());

        this.updateAllAgentsInDb();
        logger.debug("MRD has been saved as Associated MRD for all agents in DB | MRD: {}", inserted.getId());

        // Insert in MRD config DB
        logger.info("MRD successfully created | MRD: {}", inserted.getId());
        return inserted;
    }

    @Override
    public ResponseEntity<Object> retrieve(String mrdId) {
        if (mrdId != null && !repository.existsById(mrdId)) {
            String errorMessage = "Could not find the MRD resource with id: " + mrdId;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        if (mrdId != null && repository.existsById(mrdId)) {
            MediaRoutingDomain mediaRoutingDomain = this.repository.findById(mrdId).get();
            logger.debug("MRD existed in DB. | MRD: {}", mediaRoutingDomain);
            return new ResponseEntity<>(mediaRoutingDomain, HttpStatus.OK);
        }

        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> update(MediaRoutingDomain mediaRoutingDomain, String id) {
        logger.info("Update MRD request initiated for MRD: {}", id);

        if (!this.repository.existsById(id)) {
            String errorMessage = "Could not find the MRD resource to update with id: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        if (id.equals(Constants.VOICE_MRD_ID)) {
            String errorMessage = "Update operation is forbidden for the VOICE MRD";
            logger.error(errorMessage);
            throw new ForbiddenException(errorMessage);
        }

        mediaRoutingDomain.setId(id);
        List<CCUser> agentsWithConflictedMaxTask =
                getAgentsWithConflictedMaxTasks(id, mediaRoutingDomain.getMaxRequests());

        if (!agentsWithConflictedMaxTask.isEmpty()) {
            logger.debug("agentsWithConflictedMaxTask = {} against MRD ID : {}", agentsWithConflictedMaxTask.size(),
                    id);
            String reason = new StringBuilder("Failed to update the MRD : ").append(mediaRoutingDomain.getName())
                    .append(".Because the following agents have MaxTask against this MRD which are greater than ")
                    .append("the new MRD maxRequest value i.e. ").append(mediaRoutingDomain.getMaxRequests())
                    .append(".Please update the agentMaxTask before updating the MRD MaxRequest.").toString();

            return new ResponseEntity<>(new MrdUpdateConflictResponse(mediaRoutingDomain.getName(), reason,
                    agentsWithConflictedMaxTask), HttpStatus.CONFLICT);
        }

        this.updatePrecisionQueues(mediaRoutingDomain, id);
        logger.debug("MRD updated in precision-queues inside PrecisionQueue Config DB | MRD: {}", id);

        this.mrdPool.update(mediaRoutingDomain);
        logger.debug("MRD updated in in-memory MRD pool | MRD: {}", id);

        updateMrdInAgentMrdStateInAllAgentPresence(mediaRoutingDomain);
        logger.debug("MRD updated in AgentMrdState for Agents in Agent Presence Repository | MRD: {}", id);

        updateMrdInTasks(mediaRoutingDomain);
        logger.debug("MRD updated in Tasks inside Tasks Repository | MRD: {}", id);
        // Update MRD in MRD Config DB
        MediaRoutingDomain savedInDb = this.repository.save(mediaRoutingDomain);
        logger.debug("MRD updated in MRD Config DB");

        logger.info("MRD updated successfully | MRD: {}", id);
        return new ResponseEntity<>(savedInDb, HttpStatus.OK);
    }

    /**
     * Update mrd in tasks.
     *
     * @param mediaRoutingDomain the media routing domain
     */
    void updateMrdInTasks(MediaRoutingDomain mediaRoutingDomain) {
        Map<String, TaskDto> taskMap = new HashMap<>();
        for (TaskDto taskDto : this.tasksRepository.findAll()) {
            if (taskDto.getMrd().getId().equals(mediaRoutingDomain.getId())) {
                taskDto.setMrd(mediaRoutingDomain);
                taskMap.put(taskDto.getId().toString(), taskDto);
            }
        }
        this.tasksRepository.saveAllByKeyValueMap(taskMap);
    }

    /**
     * Update mrd in agent mrd state in all agent presence.
     *
     * @param mediaRoutingDomain the media routing domain
     */
    void updateMrdInAgentMrdStateInAllAgentPresence(MediaRoutingDomain mediaRoutingDomain) {
        Map<String, AgentPresence> agentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : this.agentPresenceRepository.findAll()) {
            for (AgentMrdState agentMrdState : agentPresence.getAgentMrdStates()) {
                if (agentMrdState.getMrd().getId().equals(mediaRoutingDomain.getId())) {
                    agentMrdState.setMrd(mediaRoutingDomain);
                    agentPresenceMap.put(agentPresence.getAgent().getId().toString(), agentPresence);
                    break;
                }
            }
        }
        this.agentPresenceRepository.saveAllByKeyValueMap(agentPresenceMap);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> delete(String id) {
        logger.info("Delete MRD request initiated | MRD: {}", id);

        if (!this.repository.existsById(id)) {
            String errorMessage = "Could not find the MRD resource to delete | MRD: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        if (id.equals(Constants.VOICE_MRD_ID)) {
            String errorMessage = "Delete operation is forbidden for the VOICE MRD";
            logger.error(errorMessage);
            throw new ForbiddenException(errorMessage);
        }

        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueRepository.findByMrdId(id);
        List<Task> tasks = this.tasksPool.findByMrdId(id);

        if (precisionQueueEntities.isEmpty() && tasks.isEmpty()) {
            for (Agent agent : this.agentsPool.findAll()) {
                agent.deleteAgentMrdState(id);
            }
            logger.debug("AgentMrdState deleted from Agents in in-memory Agents pool | MRD: {}", id);

            deleteAgentMrdStateFromAllAgentPresence(id);
            logger.debug("AgentMrdState deleted from Agents in Agent Presence Repository | MRD: {}", id);

            this.updateAllAgentsInDb();
            logger.debug("AssociatedMrd deleted for all Agents in DB | MRD: {}", id);

            this.mrdPool.deleteById(id);
            logger.debug("MRD deleted from in-memory MRD pool | MRD: {}", id);

            // Delete MRD from MRD config DB
            this.repository.deleteById(id);
            logger.debug("MRD deleted from MRD Config DB | MRD: {}", id);

            logger.info("MRD deleted successfully | MRD: {}", id);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
        }

        logger.info("Could not delete MRD: {}. It is associated to queues or tasks", id);
        List<TaskDto> taskDtoList = new ArrayList<>();
        tasks.forEach(task -> taskDtoList.add(AdapterUtility.createTaskDtoFrom(task)));
        return new ResponseEntity<>(new MrdDeleteConflictResponse(precisionQueueEntities, taskDtoList),
                HttpStatus.CONFLICT);
    }

    /**
     * Delete agent mrd state from all agent presence.
     *
     * @param mrdId the mrd id
     */
    void deleteAgentMrdStateFromAllAgentPresence(String mrdId) {
        Map<String, AgentPresence> agentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : this.agentPresenceRepository.findAll()) {
            deleteAgentMrdStateFromAgentPresence(mrdId, agentPresence);
            agentPresenceMap.put(agentPresence.getAgent().getId().toString(), agentPresence);
        }
        this.agentPresenceRepository.saveAllByKeyValueMap(agentPresenceMap);
    }

    /**
     * Delete agent mrd state from agent presence.
     *
     * @param mrdId         the mrd id
     * @param agentPresence the agent presence
     */
    void deleteAgentMrdStateFromAgentPresence(String mrdId, AgentPresence agentPresence) {
        List<AgentMrdState> agentMrdStates = agentPresence.getAgentMrdStates();
        int index = -1;
        for (int i = 0; i < agentMrdStates.size(); i++) {
            if (agentMrdStates.get(i).getMrd().getId().equals(mrdId)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            agentMrdStates.remove(index);
        }
    }

    /**
     * Update precision queues.
     *
     * @param mediaRoutingDomain the media routing domain
     * @param id                 the id
     */
    void updatePrecisionQueues(MediaRoutingDomain mediaRoutingDomain, String id) {
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueRepository.findByMrdId(id);
        if (precisionQueueEntities != null && !precisionQueueEntities.isEmpty()) {
            for (PrecisionQueueEntity precisionQueueEntity : precisionQueueEntities) {
                precisionQueueEntity.setMrd(mediaRoutingDomain);
            }
            this.precisionQueueRepository.saveAll(precisionQueueEntities);
        }
    }

    /**
     * This method will return a list of Agents ,
     * who's maxAgentTask against the MRD (which is supposed to be updated) is greater than the maxMrdRequest.
     */
    List<CCUser> getAgentsWithConflictedMaxTasks(String mrdId, int maxRequest) {
        List<CCUser> agentsWithConflictedMaxTasks = new ArrayList<>();
        List<Agent> agents = this.agentsPool.findAll();

        agents.forEach(agent -> {
            AgentMrdState agentMrdState = agent.getAgentMrdState(mrdId);
            if (agentMrdState != null
                    && isMaxAgentTasksGreaterThanMrdMaxRequestValue(agent.getId(), agentMrdState.getMaxAgentTasks(),
                    maxRequest)) {
                agentsWithConflictedMaxTasks.add(agent.toCcUser());
            }
        });

        return agentsWithConflictedMaxTasks;
    }

    /**
     * This method will return TRUE
     * if an agent's maxTasks value against an MRD is greater than new MRDs maxRequest Value.
     */
    boolean isMaxAgentTasksGreaterThanMrdMaxRequestValue(UUID agentId, int maxAgentTask, int mrdMaxRequest) {
        logger.trace("isMaxAgentTasksGreaterThanMrdMaxRequestValue started. |");
        if (maxAgentTask > mrdMaxRequest) {
            logger.info("The agent ID : {} has maxAgentTask = {} which are greater than mrdMaxRequest = {}", agentId,
                    maxAgentTask, mrdMaxRequest);
            return true;
        }
        return false;
    }

    /**
     * This will update all agents in DB.
     */
    void updateAllAgentsInDb() {
        List<Agent> agents = this.agentsPool.findAll();
        agents.forEach(agent -> agentsService.saveUpdatedAgentInDb(agent.toCcUser()));
        logger.debug("All Agents has been updated in DB. |");
    }
}