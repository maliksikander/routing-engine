package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Mrd Update conflict response.
 */
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MrdUpdateConflictResponse {
    private String mrdName;
    private String reason;
    private List<CCUser> agents;
}