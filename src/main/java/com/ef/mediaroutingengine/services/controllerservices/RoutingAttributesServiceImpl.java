package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.dto.RoutingAttributeDeleteConflictResponse;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.Expression;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.Term;
import com.ef.mediaroutingengine.repositories.AgentsRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoutingAttributesServiceImpl implements RoutingAttributesService {
    private final RoutingAttributeRepository repository;
    private final PrecisionQueueRepository precisionQueueRepository;
    private final AgentsRepository agentsRepository;

    @Autowired
    public RoutingAttributesServiceImpl(RoutingAttributeRepository repository,
                                        PrecisionQueueRepository precisionQueueRepository,
                                        AgentsRepository agentsRepository) {
        this.repository = repository;
        this.precisionQueueRepository = precisionQueueRepository;
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
    public RoutingAttribute update(RoutingAttribute routingAttribute, UUID id) {
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
        List<PrecisionQueue> precisionQueues = this.precisionQueueRepository.findByRoutingAttributeId(id);
        List<CCUser> agents = this.agentsRepository.findByRoutingAttributeId(id);
        if(precisionQueues.isEmpty() && agents.isEmpty()) {
            repository.deleteById(id);
            return null;
        }
        RoutingAttributeDeleteConflictResponse response = new RoutingAttributeDeleteConflictResponse();
        response.setAgents(agents);
        response.setPrecisionQueues(precisionQueues);
        return response;
    }

    private void updatePrecisionQueues(RoutingAttribute routingAttribute, UUID id) {
        List<PrecisionQueue> precisionQueues = this.precisionQueueRepository.findByRoutingAttributeId(id);
        if (precisionQueues != null && !precisionQueues.isEmpty()) {
            for (PrecisionQueue precisionQueue : precisionQueues) {
                List<Step> steps = precisionQueue.getSteps();
                if (steps == null) continue;
                for (Step step : steps) {
                    List<Expression> expressions = step.getExpressions();
                    if (expressions == null) continue;
                    for (Expression expression : expressions) {
                        List<Term> terms = expression.getTerms();
                        if (terms == null) continue;
                        for (Term term : terms) {
                            RoutingAttribute existingRoutingAttribute = term.getRoutingAttribute();
                            if (existingRoutingAttribute == null) continue;
                            if (existingRoutingAttribute.getId().equals(id)) {
                                term.setRoutingAttribute(routingAttribute);
                            }
                        }
                    }
                }
            }
            this.precisionQueueRepository.saveAll(precisionQueues);
        }
    }

    private void updateAgents(RoutingAttribute routingAttribute, UUID id) {
        List<CCUser> agents = this.agentsRepository.findByRoutingAttributeId(id);
        if(agents == null || agents.isEmpty()){
            return;
        }

        for(CCUser agent: agents){
            List<AssociatedRoutingAttribute> associatedRoutingAttributes = agent.getAssociatedRoutingAttributes();
            if(associatedRoutingAttributes==null) continue;
            for(AssociatedRoutingAttribute associatedRoutingAttribute: associatedRoutingAttributes){
                RoutingAttribute existingRoutingAttribute = associatedRoutingAttribute.getRoutingAttribute();
                if(existingRoutingAttribute == null) continue;
                if(existingRoutingAttribute.getId().equals(id)){
                    associatedRoutingAttribute.setRoutingAttribute(routingAttribute);
                }
            }
        }

        this.agentsRepository.saveAll(agents);
    }
}
