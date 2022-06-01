package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import java.util.List;
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
    List<MediaRoutingDomain> retrieve();

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
