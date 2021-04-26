package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.ExpressionEntity;
import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.StepEntity;
import com.ef.mediaroutingengine.model.TermEntity;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import com.ef.mediaroutingengine.services.PrecisionQueuesPool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrecisionQueueEntityServiceImpl implements PrecisionQueueEntityService {

    private final PrecisionQueueEntityRepository repository;
    private final MediaRoutingDomainRepository mrdRepository;
    private final RoutingAttributeRepository routingAttributeRepository;
    private final PrecisionQueuesPool precisionQueuesPool;

    /**
     * Default constructor.
     *
     * @param repository                 precision queue repository
     * @param mrdRepository              media routing domain's repository
     * @param routingAttributeRepository routing attribute's repository
     */
    @Autowired
    public PrecisionQueueEntityServiceImpl(PrecisionQueueEntityRepository repository,
                                           MediaRoutingDomainRepository mrdRepository,
                                           RoutingAttributeRepository routingAttributeRepository,
                                           PrecisionQueuesPool precisionQueuesPool) {
        this.repository = repository;
        this.mrdRepository = mrdRepository;
        this.routingAttributeRepository = routingAttributeRepository;
        this.precisionQueuesPool = precisionQueuesPool;
    }

    @Override
    public PrecisionQueueEntity create(PrecisionQueueEntity precisionQueueEntity) throws Exception {
        this.validateAndSetMRD(precisionQueueEntity);
        precisionQueueEntity.setId(UUID.randomUUID());
        PrecisionQueueEntity saved = repository.insert(precisionQueueEntity);
        //this.precisionQueuesPool.add(saved);
        return saved;
    }

    @Override
    public List<PrecisionQueueEntity> retrieve() {
        return this.repository.findAll();
    }

    @Override
    public PrecisionQueueEntity update(PrecisionQueueEntity precisionQueueEntity, UUID id) throws Exception {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find precision queue resource to update");
        }
        this.validateAndSetMRD(precisionQueueEntity);
        this.validateAndSetRoutingAttributes(precisionQueueEntity);
        precisionQueueEntity.setId(id);
        PrecisionQueueEntity saved = this.repository.save(precisionQueueEntity);
        this.precisionQueuesPool.evaluateAssociatedAgents(saved);
        return saved;
    }

    @Override
    public void delete(UUID id) throws Exception {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find precision resource to delete");
        }
        this.repository.deleteById(id);
        this.precisionQueuesPool.remove(id);
    }

    private void validateAndSetMRD(PrecisionQueueEntity pq) {
        NotFoundException notFoundException = new NotFoundException(
                "Could not find media-routing-domain resource");
        if (pq.getMrd().getId() == null) {
            throw notFoundException;
        }

        Optional<MediaRoutingDomain> optionalMrd = mrdRepository.findById(pq.getMrd().getId());

        if (!optionalMrd.isPresent()) {
            throw notFoundException;
        }
        pq.setMrd(optionalMrd.get());
    }

    private Map<UUID, RoutingAttribute> retrieveRoutingAttributes() {
        List<RoutingAttribute> routingAttributes = routingAttributeRepository.findAll();
        Map<UUID, RoutingAttribute> routingAttributeMap = new HashMap<>();
        for (RoutingAttribute routingAttribute : routingAttributes) {
            routingAttributeMap.put(routingAttribute.getId(), routingAttribute);
        }
        return routingAttributeMap;
    }

    private void validateAndSetRoutingAttributes(PrecisionQueueEntity pq) {
        List<StepEntity> stepEntities = pq.getSteps();
        if (stepEntities == null || stepEntities.isEmpty()) {
            return;
        }

        Map<UUID, RoutingAttribute> routingAttributes = this.retrieveRoutingAttributes();

        for (StepEntity stepEntity : stepEntities) {
            List<ExpressionEntity> expressionEntities = stepEntity.getExpressions();
            if (expressionEntities == null) {
                continue;
            }
            for (ExpressionEntity expressionEntity : expressionEntities) {
                List<TermEntity> termEntities = expressionEntity.getTerms();
                if (termEntities == null) {
                    continue;
                }
                for (TermEntity termEntity : termEntities) {
                    RoutingAttribute requestRoutingAttribute = termEntity.getRoutingAttribute();
                    if (requestRoutingAttribute == null) {
                        continue;
                    }

                    if (!routingAttributes.containsKey(requestRoutingAttribute.getId())) {
                        throw new NotFoundException("Could not find a routing-attribute resource");
                    }

                    termEntity.setRoutingAttribute(
                            routingAttributes.get(requestRoutingAttribute.getId()));
                }
            }
        }
    }
}
