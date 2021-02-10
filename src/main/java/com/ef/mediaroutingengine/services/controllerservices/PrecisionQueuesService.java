package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.PrecisionQueue;

import java.util.List;
import java.util.UUID;

public interface PrecisionQueuesService {
    PrecisionQueue create(PrecisionQueue precisionQueue);
    List<PrecisionQueue> retrieve();
    PrecisionQueue update(PrecisionQueue precisionQueue, UUID id);
    void delete(UUID id);
}
