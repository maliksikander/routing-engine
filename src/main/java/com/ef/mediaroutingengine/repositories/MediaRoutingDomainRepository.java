package com.ef.mediaroutingengine.repositories;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Media routing domain repository.
 */
@Repository
public interface MediaRoutingDomainRepository extends MongoRepository<MediaRoutingDomain, UUID> {

    /**
     * Find by name media routing domain.
     *
     * @param name the name
     * @return the media routing domain
     */
    MediaRoutingDomain findByName(String name);

    //MediaRoutingDomain findById(UUID id);
    boolean existsById(UUID id);
}
