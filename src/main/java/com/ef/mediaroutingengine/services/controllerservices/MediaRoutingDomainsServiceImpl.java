package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.MrdDeleteConflictResponse;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.AgentMrdState;
import com.ef.mediaroutingengine.model.AgentPresence;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.AgentPresenceRepository;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                                          TasksRepository tasksRepository) {
        this.repository = repository;
        this.precisionQueueRepository = precisionQueueRepository;
        this.tasksPool = tasksPool;
        this.mrdPool = mrdPool;
        this.agentsPool = agentsPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.tasksRepository = tasksRepository;
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
        // Insert in MRD config DB
        logger.info("MRD successfully created | MRD: {}", inserted.getId());
        return inserted;
    }

    @Override
    public List<MediaRoutingDomain> retrieve() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public MediaRoutingDomain update(MediaRoutingDomain mediaRoutingDomain, String id) {
        logger.info("Update MRD request initiated for MRD: {}", id);

        if (!this.repository.existsById(id)) {
            String errorMessage = "Could not find the MRD resource to update with id: " + id;
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        mediaRoutingDomain.setId(id);

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
        return savedInDb;
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

        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueRepository.findByMrdId(id);
        List<Task> tasks = this.tasksPool.findByMrdId(id);

        if (precisionQueueEntities.isEmpty() && tasks.isEmpty()) {
            for (Agent agent : this.agentsPool.findAll()) {
                agent.deleteAgentMrdState(id);
            }
            logger.debug("AgentMrdState deleted from Agents in in-memory Agents pool | MRD: {}", id);

            deleteAgentMrdStateFromAllAgentPresence(id);
            logger.debug("AgentMrdState deleted from Agents in Agent Presence Repository | MRD: {}", id);

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
        tasks.forEach(task -> taskDtoList.add(new TaskDto(task)));
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
}