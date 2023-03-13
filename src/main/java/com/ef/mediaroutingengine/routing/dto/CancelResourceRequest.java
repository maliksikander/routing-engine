package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.Enums;
import javax.validation.constraints.NotNull;

/**
 * The type End task request.
 */
public class CancelResourceRequest {
    /**
     * The Topic id.
     */
    @NotNull(message = "topicId cannot be null")
    private String topicId;
    /**
     * The Reason code.
     */
    private Enums.TaskStateReasonCode reasonCode;

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Sets topic id.
     *
     * @param topicId the topic id
     */
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    /**
     * Gets reason code.
     *
     * @return the reason code
     */
    public Enums.TaskStateReasonCode getReasonCode() {
        return reasonCode;
    }

    /**
     * Sets reason code.
     *
     * @param reasonCode the reason code
     */
    public void setReasonCode(Enums.TaskStateReasonCode reasonCode) {
        this.reasonCode = reasonCode == null ? Enums.TaskStateReasonCode.CANCELLED : reasonCode;
    }
}
