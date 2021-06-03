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
import com.ef.mediaroutingengine.repositories.PrecisionQueueEntityRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingAttributesServiceImpl implements RoutingAttributesService {

    private final RoutingAttributeRepository repository;
    private final PrecisionQueueEntityRepository precisionQueueEntityRepository;
    private final AgentsRepository agentsRepository;

    /**
     * Default constructor.
     *
     * @param repository                     routing attribute repository
     * @param precisionQueueEntityRepository precision queue repository
     * @param agentsRepository               agents repository
     */
    @Autowired
    public RoutingAttributesServiceImpl(RoutingAttributeRepository repository,
                                        PrecisionQueueEntityRepository precisionQueueEntityRepository,
                                        AgentsRepository agentsRepository) {
        this.repository = repository;
        this.precisionQueueEntityRepository = precisionQueueEntityRepository;
        this.agentsRepository = agentsRepository;
    }

    @Override
    public RoutingAttribute create(RoutingAttribute routingAttribute) {
        routingAttribute.setId(UUID.randomUUID());
        return repository.insert(routingAttribute);
    }

    @Override
    public List<RoutingAttribute> retrieve() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public RoutingAttribute update(RoutingAttribute routingAttribute, UUID id) throws Exception {
        if (!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find resource to update");
        }

        routingAttribute.setId(id);

        this.updatePrecisionQueues(routingAttribute, id);
        this.updateAgents(routingAttribute, id);
        return repository.save(routingAttribute);
    }

    @Override
    @Transactional
    public RoutingAttributeDeleteConflictResponse delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Could not find resource to delete");
        }
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueEntityRepository
                .findByRoutingAttributeId(id);
        List<CCUser> agents = this.agentsRepository.findByRoutingAttributeId(id);
        if (precisionQueueEntities.isEmpty() && agents.isEmpty()) {
            repository.deleteById(id);
            return null;
        }
        RoutingAttributeDeleteConflictResponse response = new RoutingAttributeDeleteConflictResponse();
        response.setAgents(agents);
        response.setPrecisionQueues(precisionQueueEntities);
        return response;
    }

    private void updatePrecisionQueues(RoutingAttribute routingAttribute, UUID id) {
        List<PrecisionQueueEntity> precisionQueueEntities = this.precisionQueueEntityRepository
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
            this.precisionQueueEntityRepository.saveAll(precisionQueueEntities);
        }
    }

    private void updateAgents(RoutingAttribute routingAttribute, UUID id) {
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
