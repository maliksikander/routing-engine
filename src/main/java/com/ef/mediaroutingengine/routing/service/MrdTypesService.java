package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.MrdType;
import com.ef.mediaroutingengine.routing.repository.MrdTypeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Mrd types service.
 */
@Service
public class MrdTypesService {
    /**
     * The Repository.
     */
    private final MrdTypeRepository repository;

    /**
     * Instantiates a new Mrd types service.
     *
     * @param repository the repository
     */
    @Autowired
    public MrdTypesService(MrdTypeRepository repository) {
        this.repository = repository;
    }

    /**
     * Gets mrd types.
     *
     * @return the mrd types
     */
    public List<MrdType> getMrdTypes() {
        return this.repository.findAll();
    }
}
