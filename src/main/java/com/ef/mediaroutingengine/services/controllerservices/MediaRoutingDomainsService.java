package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import java.util.List;
import java.util.UUID;

/**
 * The interface Media routing domains service.
 */
public interface MediaRoutingDomainsService {

    /**
     * Create media routing domain.
     *
     * @param mediaRoutingDomain the media routing domain
     * @return the media routing domain
     */
    MediaRoutingDomain create(MediaRoutingDomain mediaRoutingDomain);

    /**
     * Retrieve list.
     *
     * @return the list
     */
    List<MediaRoutingDomain> retrieve();

    /**
     * Update media routing domain.
     *
     * @param mediaRoutingDomain the media routing domain
     * @param id                 the id
     * @return the media routing domain
     * @throws Exception the exception
     */
    MediaRoutingDomain update(MediaRoutingDomain mediaRoutingDomain, UUID id) throws Exception;

    /**
     * Delete list.
     *
     * @param id the id
     * @return the list
     */
    List<PrecisionQueueEntity> delete(UUID id);
}
