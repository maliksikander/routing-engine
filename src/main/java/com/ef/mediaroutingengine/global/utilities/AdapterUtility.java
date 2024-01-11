package com.ef.mediaroutingengine.global.utilities;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.Sender;
import com.ef.cim.objectmodel.SenderType;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;

/**
 * The type Adapter utility.
 */
public final class AdapterUtility {
    /**
     * Instantiates a new Adapter utility.
     */
    private AdapterUtility() {

    }

    /**
     * To precision queue entity precision queue entity.
     *
     * @param requestBody the request body
     * @return the precision queue entity
     */
    public static PrecisionQueueEntity createQueueEntityFrom(PrecisionQueueRequestBody requestBody) {
        PrecisionQueueEntity entity = new PrecisionQueueEntity();
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
        entity.setAgentSlaDuration(requestBody.getAgentSlaDuration());
        return entity;
    }

    /**
     * Update queue.
     *
     * @param entity      the entity
     * @param requestBody the request body
     */
    public static void updateQueueEntityFrom(PrecisionQueueRequestBody requestBody, PrecisionQueueEntity entity) {
        entity.setName(requestBody.getName());
        entity.setMrd(requestBody.getMrd());
        entity.setServiceLevelType(requestBody.getServiceLevelType());
        entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());
        entity.setAgentSlaDuration(requestBody.getAgentSlaDuration());
    }

    public static Sender getSender() {
        return new Sender("86e3d082-a904-11ed-afa1-0242ac120002", SenderType.SYSTEM,
                "ROUTING_ENGINE", null);
    }
}