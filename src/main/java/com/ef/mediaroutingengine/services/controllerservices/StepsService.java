package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.model.PrecisionQueueEntity;
import com.ef.mediaroutingengine.model.StepEntity;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface StepsService {
    PrecisionQueueEntity create(UUID queueId, StepEntity stepEntity);
    PrecisionQueueEntity update(UUID id, UUID queueId, StepEntity stepEntity);
    ResponseEntity<Object> delete(UUID queueId, UUID id);
}
