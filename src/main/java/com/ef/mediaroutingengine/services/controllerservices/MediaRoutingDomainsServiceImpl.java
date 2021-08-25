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
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import com.ef.mediaroutingengine.services.pools.AgentsPool;
import com.ef.mediaroutingengine.services.pools.MrdPool;
import com.ef.mediaroutingengine.services.pools.TasksPool;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaRoutingDomainsServiceImpl.class);
    /**
     * The Repository.
     */
    private final MediaRoutingDomainRepository repository;
    /**
     * The Precision queue entity repository.
     */
    private final PrecisionQueueEntityRepository precisionQueueEntityRepository;

    private final TasksPool tasksPool;

    private final MrdPool mrdPool;

    private final AgentsPool agentsPool;

    private final AgentPresenceRepository agentPresenceRepository;

    private final TasksRepository tasksRepository;

    /**
     * Constructor, Autowired, loads the beans.
     *
     * @param repository                     to communicate with MRD collection in DB
     * @param precisionQueueEntityRepository to communicate with PrecisionQueues collection in DB
     */
    @Autowired
    public MediaRoutingDomainsServiceImpl(MediaRoutingDomainRepository repository,
                                          PrecisionQueueEntityRepository precisionQueueEntityRepository,
                                          TasksPool tasksPool, MrdPool mrdPool, AgentsPool agentsPool,
                                          AgentPresenceRepository agentPresenceRepository,
                                          TasksRepository tasksRepository) {
        this.repository = repository;
        this.precisionQueueEntityRepository = precisionQueueEntityRepository;
        this.tasksPool = tasksPool;
        this.mrdPool = mrdPool;
        this.agentsPool = agentsPool;
        this.agentPresenceRepository = agentPresenceRepository;
        this.tasksRepository = tasksRepository;
    }

    @Override
    public MediaRoutingDomain create(MediaRoutingDomain mediaRoutingDomain) {
        mediaRoutingDomain.setId(UUID.randomUUID());
        // Insert in in-memory pool
        this.mrdPool.insert(mediaRoutingDomain);
        // Add an AgentMrdState for this MRD in every agent in in-memory pool.
        AgentMrdState agentMrdState = new AgentMrdState(mediaRoutingDomain, Enums.AgentMrdStateName.NOT_READY);
        for (Agent agent : agentsPool.findAll()) {
            agent.addAgentMrdState(agentMrdState);
        }
        // Add an AgentMrdState for this MRD for every agent in Agent-Presence Collection.
        Map<String, AgentPresence> agentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : this.agentPresenceRepository.findAll()) {
            agentPresence.getAgentMrdStates().add(agentMrdState);
            agentPresenceMap.put(agentPresence.getAgent().getId().toString(), agentPresence);
        }
        this.agentPresenceRepository.saveAllByKeyValueMap(agentPresenceMap);
        // Insert in MRD config DB
        return repository.insert(mediaRoutingDomain);
    }

    @Override
    public List<MediaRoutingDomain> retrieve() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public MediaRoutingDomain update(MediaRoutingDomain mediaRoutingDomain, UUID id) throws Exception {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find the resource to update");
        }
        mediaRoutingDomain.setId(id);
        // Update MRD in precision-queues in Config DB
        this.updatePrecisionQueues(mediaRoutingDomain, id);
        // Update MRD in in-memory MRD-pool
        this.mrdPool.update(mediaRoutingDomain);
        // Update MRD in AgentMrdStates in AgentPresence in the Redis AgentPresence Collection
        updateMrdInAgentMrdStateInAllAgentPresence(mediaRoutingDomain);
        // Update MRD in Tasks in the Redis Tasks Collection
        updateMrdInTasks(mediaRoutingDomain);
        // Update MRD in MRD Config DB
        return this.repository.save(mediaRoutingDomain);
    }

    private void updateMrdInTasks(MediaRoutingDomain mediaRoutingDomain) {
        Map<String, TaskDto> taskMap = new HashMap<>();
        for (TaskDto taskDto: this.tasksRepository.findAll()) {
            if (taskDto.getMrd().getId().equals(mediaRoutingDomain.getId())) {
                taskDto.setMrd(mediaRoutingDomain);
                taskMap.put(taskDto.getId().toString(), taskDto);
            }
        }
        this.tasksRepository.saveAllByKeyValueMap(taskMap);
    }

    private void updateMrdInAgentMrdStateInAllAgentPresence(MediaRoutingDomain mediaRoutingDomain) {
        Map<String, AgentPresence> agentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence : this.agentPresenceRepository.findAll()) {
            for (AgentMrdState agentMrdState: agentPresence.getAgentMrdStates()) {
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
    public ResponseEntity<Object> delete(UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find the resource to delete");
        }
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueEntityRepository.findByMrdId(id);
        List<Task> tasks = this.tasksPool.findByMrdId(id);
        if (precisionQueueEntities.isEmpty() && tasks.isEmpty()) {
            // Delete AgentMrdState for this MRD from all agents in in-memory Agents-pool
            for (Agent agent: this.agentsPool.findAll()) {
                agent.deleteAgentMrdState(id);
            }
            // Delete AgentMrdState for this MRD from all AgentPresence in Redis Agent-Presence Collection
            deleteAgentMrdStateFromAllAgentPresence(id);
            // Delete MRD from in-memory MRD-pool
            this.mrdPool.deleteById(id);
            // Delete MRD from MRD config DB
            this.repository.deleteById(id);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
        }
        LOGGER.debug("Could not delete MRD: {}. It is associated with one or more Queues", id);
        return new ResponseEntity<>(new MrdDeleteConflictResponse(precisionQueueEntities, tasks), HttpStatus.CONFLICT);
    }

    private void deleteAgentMrdStateFromAllAgentPresence(UUID mrdId) {
        Map<String, AgentPresence> agentPresenceMap = new HashMap<>();
        for (AgentPresence agentPresence: this.agentPresenceRepository.findAll()) {
            deleteAgentMrdStateFromAgentPresence(mrdId, agentPresence);
            agentPresenceMap.put(agentPresence.getAgent().getId().toString(), agentPresence);
        }
        this.agentPresenceRepository.saveAllByKeyValueMap(agentPresenceMap);
    }

    private void deleteAgentMrdStateFromAgentPresence(UUID mrdId, AgentPresence agentPresence) {
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
    private void updatePrecisionQueues(MediaRoutingDomain mediaRoutingDomain, UUID id) {
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueEntityRepository.findByMrdId(id);
        if (precisionQueueEntities != null && !precisionQueueEntities.isEmpty()) {
            for (PrecisionQueueEntity precisionQueueEntity : precisionQueueEntities) {
                precisionQueueEntity.setMrd(mediaRoutingDomain);
            }
            this.precisionQueueEntityRepository.saveAll(precisionQueueEntities);
        }
    }
}