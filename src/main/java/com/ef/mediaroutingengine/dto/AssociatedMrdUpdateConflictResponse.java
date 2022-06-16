package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.AssociatedMrd;
import java.util.List;
import java.util.UUID;
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
    private UUID agentId;
    private String reason;
    private List<AssociatedMrd> associatedMrds;
}