package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.mediaroutingengine.exceptions.NotFoundException;
import com.ef.mediaroutingengine.model.*;
import com.ef.mediaroutingengine.repositories.MediaRoutingDomainRepository;
import com.ef.mediaroutingengine.repositories.PrecisionQueueRepository;
import com.ef.mediaroutingengine.repositories.RoutingAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrecisionQueuesServiceImpl implements PrecisionQueuesService{
    private final PrecisionQueueRepository repository;
    private final MediaRoutingDomainRepository mrdRepository;
    private final RoutingAttributeRepository routingAttributeRepository;

    @Autowired
    public PrecisionQueuesServiceImpl(PrecisionQueueRepository repository,
                                      MediaRoutingDomainRepository mrdRepository,
                                      RoutingAttributeRepository routingAttributeRepository){
        this.repository = repository;
        this.mrdRepository = mrdRepository;
        this.routingAttributeRepository = routingAttributeRepository;
    }

    @Override
    public PrecisionQueue create(PrecisionQueue precisionQueue) {
        this.validateAndSetMRD(precisionQueue);
        precisionQueue.setId(UUID.randomUUID());
        return repository.insert(precisionQueue);
    }

    @Override
    public List<PrecisionQueue> retrieve() {
        return this.repository.findAll();
    }

    @Override
    public PrecisionQueue update(PrecisionQueue precisionQueue, UUID id) {
        if(!this.repository.existsById(id)) {
            throw new NotFoundException("Could not find precision queue resource to update");
        }
        this.validateAndSetMRD(precisionQueue);
        this.validateAndSetRoutingAttributes(precisionQueue);
        precisionQueue.setId(id);

        return this.repository.save(precisionQueue);
    }

    @Override
    public void delete(UUID id) {
        if(!this.repository.existsById(id)){
            throw new NotFoundException("Could not find precision resource to delete");
        }
        this.repository.deleteById(id);
    }

    private void validateAndSetMRD(PrecisionQueue pq) {
        NotFoundException notFoundException = new NotFoundException("Could not find media-routing-domain resource");
        if (pq.getMrd().getId() == null) {
            throw notFoundException;
        }

        Optional<MediaRoutingDomain> optionalMrd = mrdRepository.findById(pq.getMrd().getId());

        if (!optionalMrd.isPresent()) {
            throw notFoundException;
        }
        pq.setMrd(optionalMrd.get());
    }

    private Map<UUID, RoutingAttribute> retrieveRoutingAttributes(){
        List<RoutingAttribute> routingAttributes = routingAttributeRepository.findAll();
        Map<UUID, RoutingAttribute> routingAttributeMap = new HashMap<>();
        for(RoutingAttribute routingAttribute: routingAttributes){
            routingAttributeMap.put(routingAttribute.getId(), routingAttribute);
        }
        return routingAttributeMap;
    }

    private void validateAndSetRoutingAttributes(PrecisionQueue pq) {
        List<Step> steps = pq.getSteps();
        if(steps == null || steps.isEmpty()) return;

        Map<UUID, RoutingAttribute> routingAttributes = this.retrieveRoutingAttributes();

        for(Step step: steps){
            List<Expression> expressions = step.getExpressions();
            if(expressions == null) continue;
            for(Expression expression: expressions){
                List<Term> terms = expression.getTerms();
                if(terms == null) continue;
                for(Term term: terms){
                    RoutingAttribute requestRoutingAttribute = term.getRoutingAttribute();
                    if(requestRoutingAttribute == null) continue;

                    if(!routingAttributes.containsKey(requestRoutingAttribute.getId())){
                        throw new NotFoundException("Could not find a routing-attribute resource");
                    }

                    term.setRoutingAttribute(routingAttributes.get(requestRoutingAttribute.getId()));
                }
            }
        }
    }
}
