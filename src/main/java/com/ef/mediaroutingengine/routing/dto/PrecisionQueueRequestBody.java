package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

/**
 * The type Precision queue request body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PrecisionQueueRequestBody {
    /**
     * The PrecisionQueue id.
     */
    @Id
    private String id;
    /**
     * The Name.
     */
    @NotNull
    @Size(min = 3, max = 50)
    private String name;
    /**
     * The Mrd.
     */
    @NotNull
    private MediaRoutingDomain mrd;
    /**
     * The Service level type.
     */
    @Min(1)
    private int serviceLevelType;
    /**
     * The Service level threshold.
     */
    @NotNull
    private int serviceLevelThreshold;
    /**
     * The Agent service level duration.
     */
    private Integer agentSlaDuration;
}
