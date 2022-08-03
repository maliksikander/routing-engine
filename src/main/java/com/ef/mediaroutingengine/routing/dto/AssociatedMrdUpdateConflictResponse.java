package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.AssociatedMrd;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Associated MRD Update Conflict Response.
 */
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AssociatedMrdUpdateConflictResponse {
    private String agentId;
    private String reason;
    private List<AssociatedMrd> associatedMrds;
}