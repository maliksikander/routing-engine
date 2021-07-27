package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Media routing domains service.
 */
@Service
public class MediaRoutingDomainsServiceImpl implements MediaRoutingDomainsService {

    /**
     * The Repository.
     */
    private final MediaRoutingDomainRepository repository;
    /**
     * The Precision queue entity repository.
     */
    private final PrecisionQueueEntityRepository precisionQueueEntityRepository;

    /**
     * Constructor, Autowired, loads the beans.
     *
     * @param repository                     to communicate with MRD collection in DB
     * @param precisionQueueEntityRepository to communicate with PrecisionQueues collection in DB
     */
    @Autowired
    public MediaRoutingDomainsServiceImpl(MediaRoutingDomainRepository repository,
                                          PrecisionQueueEntityRepository precisionQueueEntityRepository) {
        this.repository = repository;
        this.precisionQueueEntityRepository = precisionQueueEntityRepository;
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
    public List<PrecisionQueueEntity> delete(UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find the resource to delete");
        }
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueEntityRepository.findByMrdId(id);
        if (precisionQueueEntities.isEmpty()) {
            this.repository.deleteById(id);
        }
        return precisionQueueEntities;
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