package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.RoutingAttributeDeleteConflictResponse;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.ExpressionEntity;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.StepEntity;
import com.ef.mediaroutingengine.model.TermEntity;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import com.ef.mediaroutingengine.services.pools.RoutingAttributesPool;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Routing attributes service.
 */
@Service
public class RoutingAttributesServiceImpl implements RoutingAttributesService {
    /**
     * The Repository.
     */
    private final RoutingAttributeRepository repository;
    /**
     * The Routing attributes pool.
     */
    private final RoutingAttributesPool routingAttributesPool;
    /**
     * The Precision queue entity repository.
     */
    private final PrecisionQueueRepository precisionQueueRepository;
    /**
     * The Agents repository.
     */
    private final AgentsRepository agentsRepository;

    /**
     * Default constructor.
     *
     * @param repository                     routing attribute repository
     * @param routingAttributesPool          the routing attributes pool
     * @param precisionQueueRepository precision queue repository
     * @param agentsRepository               agents repository
     */
    @Autowired
    public RoutingAttributesServiceImpl(RoutingAttributeRepository repository,
                                        RoutingAttributesPool routingAttributesPool,
                                        PrecisionQueueRepository precisionQueueRepository,
                                        AgentsRepository agentsRepository) {
        this.repository = repository;
        this.routingAttributesPool = routingAttributesPool;
        this.precisionQueueRepository = precisionQueueRepository;
        this.agentsRepository = agentsRepository;
    }

    @Override
    public RoutingAttribute create(RoutingAttribute routingAttribute) {
        RoutingAttribute inserted = repository.insert(routingAttribute);
        this.routingAttributesPool.insert(inserted);
        return inserted;
    }

    @Override
    public List<RoutingAttribute> retrieve() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public RoutingAttribute update(RoutingAttribute routingAttribute, String id) {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find resource to update");
        }

        routingAttribute.setId(id);

        this.updatePrecisionQueues(routingAttribute, id);
        this.updateAgents(routingAttribute, id);
        this.routingAttributesPool.update(routingAttribute);
        return repository.save(routingAttribute);
    }

    @Override
    @Transactional
    public RoutingAttributeDeleteConflictResponse delete(String id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Could not find resource to delete");
        }
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueRepository
                .findByRoutingAttributeId(id);
        List<CCUser> agents = this.agentsRepository.findByRoutingAttributeId(id);
        if (precisionQueueEntities.isEmpty() && agents.isEmpty()) {
            this.routingAttributesPool.deleteById(id);
            repository.deleteById(id);
            return null;
        }
        RoutingAttributeDeleteConflictResponse response = new RoutingAttributeDeleteConflictResponse();
        response.setAgents(agents);
        response.setPrecisionQueues(precisionQueueEntities);
        return response;
    }

    /**
     * Update precision queues.
     *
     * @param routingAttribute the routing attribute
     * @param id               the id
     */
    private void updatePrecisionQueues(RoutingAttribute routingAttribute, String id) {
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueRepository
                .findByRoutingAttributeId(id);
        if (precisionQueueEntities != null && !precisionQueueEntities.isEmpty()) {
            for (PrecisionQueueEntity precisionQueueEntity : precisionQueueEntities) {
                List<StepEntity> stepEntities = precisionQueueEntity.getSteps();
                if (stepEntities == null) {
                    continue;
                }
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
                            RoutingAttribute existingRoutingAttribute = termEntity.getRoutingAttribute();
                            if (existingRoutingAttribute == null) {
                                continue;
                            }
                            if (existingRoutingAttribute.getId().equals(id)) {
                                termEntity.setRoutingAttribute(routingAttribute);
                            }
                        }
                    }
                }
            }
            this.precisionQueueRepository.saveAll(precisionQueueEntities);
        }
    }

    /**
     * Update agents.
     *
     * @param routingAttribute the routing attribute
     * @param id               the id
     */
    private void updateAgents(RoutingAttribute routingAttribute, String id) {
        List<CCUser> agents = this.agentsRepository.findByRoutingAttributeId(id);
        if (agents == null || agents.isEmpty()) {
            return;
        }

        for (CCUser agent : agents) {
            List<AssociatedRoutingAttribute> associatedRoutingAttributes = agent
                    .getAssociatedRoutingAttributes();
            if (associatedRoutingAttributes == null) {
                continue;
            }
            for (AssociatedRoutingAttribute associatedRoutingAttribute : associatedRoutingAttributes) {
                RoutingAttribute existingRoutingAttribute = associatedRoutingAttribute
                        .getRoutingAttribute();
                if (existingRoutingAttribute == null) {
                    continue;
                }
                if (existingRoutingAttribute.getId().equals(id)) {
                    associatedRoutingAttribute.setRoutingAttribute(routingAttribute);
                }
            }
        }

        this.agentsRepository.saveAll(agents);
    }
}
