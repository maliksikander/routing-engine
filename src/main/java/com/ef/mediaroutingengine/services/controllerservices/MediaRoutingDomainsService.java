package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import com.ef.mediaroutingengine.model.PrecisionQueue;
import java.util.List;
import java.util.UUID;

public interface MediaRoutingDomainsService {

    MediaRoutingDomain create(MediaRoutingDomain mediaRoutingDomain);

    List<MediaRoutingDomain> retrieve();

    MediaRoutingDomain update(MediaRoutingDomain mediaRoutingDomain, UUID id);

    List<PrecisionQueue> delete(UUID id);
}
