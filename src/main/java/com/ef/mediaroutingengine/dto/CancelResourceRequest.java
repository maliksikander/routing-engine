package com.ef.mediaroutingengine.dto;

import com.ef.mediaroutingengine.commons.Enums;
import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * The type End task request.
 */
public class CancelResourceRequest {
    /**
     * The Topic id.
     */
    @NotNull(message = "topicId cannot be null")
    private UUID topicId;
    /**
     * The Reason code.
     */
    private Enums.TaskStateReasonCode reasonCode;

    /**
     * Gets topic id.
     *
     * @return the topic id
     */
    public UUID getTopicId() {
        return topicId;
    }

    /**
     * Sets topic id.
     *
     * @param topicId the topic id
     */
    public void setTopicId(UUID topicId) {
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
