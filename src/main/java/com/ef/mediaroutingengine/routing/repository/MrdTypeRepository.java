package com.ef.mediaroutingengine.routing.repository;

import com.ef.cim.objectmodel.MrdType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Mrd type repository.
 */
@Repository
public interface MrdTypeRepository extends MongoRepository<MrdType, String> {

}
