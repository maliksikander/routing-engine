package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.MrdDeleteConflictResponse;
import com.ef.mediaroutingengine.dto.SuccessResponseBody;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.Task;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import com.ef.mediaroutingengine.services.pools.TasksPool;
import java.util.List;
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

    /**
     * Constructor, Autowired, loads the beans.
     *
     * @param repository                     to communicate with MRD collection in DB
     * @param precisionQueueEntityRepository to communicate with PrecisionQueues collection in DB
     */
    @Autowired
    public MediaRoutingDomainsServiceImpl(MediaRoutingDomainRepository repository,
                                          PrecisionQueueEntityRepository precisionQueueEntityRepository,
                                          TasksPool tasksPool) {
        this.repository = repository;
        this.precisionQueueEntityRepository = precisionQueueEntityRepository;
        this.tasksPool = tasksPool;
    }

    @Override
    public MediaRoutingDomain create(MediaRoutingDomain mediaRoutingDomain) {
        mediaRoutingDomain.setId(UUID.randomUUID());
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
        this.updatePrecisionQueues(mediaRoutingDomain, id);
        return this.repository.save(mediaRoutingDomain);
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
            this.repository.deleteById(id);
            return new ResponseEntity<>(new SuccessResponseBody("Successfully Deleted"), HttpStatus.OK);
        }
        LOGGER.debug("Could not delete MRD: {}. It is associated with one or more Queues", id);
        return new ResponseEntity<>(new MrdDeleteConflictResponse(precisionQueueEntities, tasks), HttpStatus.CONFLICT);
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