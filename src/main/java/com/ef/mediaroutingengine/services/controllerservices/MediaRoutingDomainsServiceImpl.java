package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaRoutingDomainsServiceImpl implements MediaRoutingDomainsService {

    private final MediaRoutingDomainRepository repository;
    private final PrecisionQueueRepository precisionQueueRepository;

    @Autowired
    public MediaRoutingDomainsServiceImpl(MediaRoutingDomainRepository repository,
                                          PrecisionQueueRepository precisionQueueRepository) {
        this.repository = repository;
        this.precisionQueueRepository = precisionQueueRepository;
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
    public MediaRoutingDomain update(MediaRoutingDomain mediaRoutingDomain, UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find the resource to update");
        }
        mediaRoutingDomain.setId(id);
        this.updatePrecisionQueues(mediaRoutingDomain, id);
        return this.repository.save(mediaRoutingDomain);
    }

    @Override
    @Transactional
    public List<PrecisionQueue> delete(UUID id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find the resource to delete");
        }
        List<PrecisionQueue> precisionQueues = this.precisionQueueRepository.findByMrdId(id);
        if (precisionQueues.isEmpty()) {
            this.repository.deleteById(id);
        }
        return precisionQueues;
    }

    private void updatePrecisionQueues(MediaRoutingDomain mediaRoutingDomain, UUID id) {
        List<PrecisionQueue> precisionQueues = this.precisionQueueRepository.findByMrdId(id);
        if (precisionQueues != null && !precisionQueues.isEmpty()) {
            for (PrecisionQueue precisionQueue : precisionQueues) {
                precisionQueue.setMrd(mediaRoutingDomain);
            }
            this.precisionQueueRepository.saveAll(precisionQueues);
        }
    }
}