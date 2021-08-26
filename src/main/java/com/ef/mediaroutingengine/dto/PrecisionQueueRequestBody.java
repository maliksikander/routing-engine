package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.model.MediaRoutingDomain;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;

public class PrecisionQueueRequestBody {
    /**
     * The Id.
     */
    @Id
    private UUID id;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    public int getServiceLevelType() {
        return serviceLevelType;
    }

    public void setServiceLevelType(int serviceLevelType) {
        this.serviceLevelType = serviceLevelType;
    }

    public int getServiceLevelThreshold() {
        return serviceLevelThreshold;
    }

    public void setServiceLevelThreshold(int serviceLevelThreshold) {
        this.serviceLevelThreshold = serviceLevelThreshold;
    }
}
