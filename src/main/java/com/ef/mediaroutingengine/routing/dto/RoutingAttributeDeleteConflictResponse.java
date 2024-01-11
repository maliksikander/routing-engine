package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Routing attribute delete conflict response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RoutingAttributeDeleteConflictResponse {
    /**
     * The Precision queue entities.
     */
    private List<PrecisionQueueEntity> precisionQueueEntities;
    /**
     * The Agents.
     */
    private List<CCUser> agents;
}
