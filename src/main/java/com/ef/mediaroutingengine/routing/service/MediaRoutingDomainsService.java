package com.ef.mediaroutingengine.routing.service;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import org.springframework.http.ResponseEntity;

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
    ResponseEntity<Object> retrieve(String mrdId);

    /**
     * Update media routing domain.
     *
     * @param mediaRoutingDomain the media routing domain
     * @param id                 the id
     * @return the media routing domain
     */
    ResponseEntity<Object> update(MediaRoutingDomain mediaRoutingDomain, String id);

    /**
     * Delete MRD.
     *
     * @param id the id
     * @return Http response entity
     */

    ResponseEntity<Object> delete(String id);
}
