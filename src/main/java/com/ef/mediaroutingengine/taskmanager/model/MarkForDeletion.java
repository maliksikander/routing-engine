package com.ef.mediaroutingengine.taskmanager.model;


import com.ef.cim.objectmodel.Enums;

/**
 * The type Mark for deletion.
 */
public class MarkForDeletion {
    /**
     * The Is marked.
     */
    private boolean isMarked;
    /**
     * The Task state.
     */
    private Enums.TaskStateReasonCode reasonCode;

    /**
     * Is marked boolean.
     *
     * @return the boolean
     */
    public boolean isMarked() {
        return isMarked;
    }

    /**
     * Sets marked.
     *
     * @param marked the marked
     */
    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    /**
     * Gets reason code.
     *
     * @return the reason code
     */
    public Enums.TaskStateReasonCode getReasonCode() {
        return this.reasonCode;
    }

    /**
     * Sets reason code.
     *
     * @param reasonCode the reason code
     */
    public void setReasonCode(Enums.TaskStateReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }
}
