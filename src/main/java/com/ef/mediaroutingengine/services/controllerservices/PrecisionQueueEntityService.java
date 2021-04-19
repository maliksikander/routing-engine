package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import java.util.List;
import java.util.UUID;

public interface PrecisionQueueEntityService {

    PrecisionQueueEntity create(PrecisionQueueEntity precisionQueueEntity) throws Exception;

    List<PrecisionQueueEntity> retrieve();

    PrecisionQueueEntity update(PrecisionQueueEntity precisionQueueEntity, UUID id) throws Exception;

    void delete(UUID id) throws Exception;
}
