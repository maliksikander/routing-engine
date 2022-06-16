package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.MediaRoutingDomain;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;

/**
 * The type Precision queue request body.
 */
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
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets mrd.
     *
     * @return the mrd
     */
    public MediaRoutingDomain getMrd() {
        return mrd;
    }

    /**
     * Sets mrd.
     *
     * @param mrd the mrd
     */
    public void setMrd(MediaRoutingDomain mrd) {
        this.mrd = mrd;
    }

    /**
     * Gets service level type.
     *
     * @return the service level type
     */
    public int getServiceLevelType() {
        return serviceLevelType;
    }

    /**
     * Sets service level type.
     *
     * @param serviceLevelType the service level type
     */
    public void setServiceLevelType(int serviceLevelType) {
        this.serviceLevelType = serviceLevelType;
    }

    /**
     * Gets service level threshold.
     *
     * @return the service level threshold
     */
    public int getServiceLevelThreshold() {
        return serviceLevelThreshold;
    }

    /**
     * Sets service level threshold.
     *
     * @param serviceLevelThreshold the service level threshold
     */
    public void setServiceLevelThreshold(int serviceLevelThreshold) {
        this.serviceLevelThreshold = serviceLevelThreshold;
    }
}
