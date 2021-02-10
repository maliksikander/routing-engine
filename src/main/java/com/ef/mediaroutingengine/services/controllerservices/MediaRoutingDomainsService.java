package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaRoutingDomainsService {
    MediaRoutingDomain create(MediaRoutingDomain mediaRoutingDomain);
    List<MediaRoutingDomain> retrieve();
    void update(MediaRoutingDomain mediaRoutingDomain, UUID id);
    void delete(UUID id);
}
