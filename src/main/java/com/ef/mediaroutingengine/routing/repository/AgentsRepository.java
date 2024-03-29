package com.ef.mediaroutingengine.routing.repository;

import com.ef.cim.objectmodel.CCUser;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * The interface Agents repository.
 */
public interface AgentsRepository extends MongoRepository<CCUser, String> {
    /**
     * Find by routing attribute id list.
     *
     * @param id the id
     * @return the list
     */
    @Query("{'associatedRoutingAttributes.routingAttribute.id': ?0}")
    List<CCUser> findByRoutingAttributeId(String id);

    boolean existsByKeycloakUserUsername(String username);

    CCUser findByKeycloakUserUsername(String username);
}
