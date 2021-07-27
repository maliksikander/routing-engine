package com.ef.mediaroutingengine.dto;

import java.sql.Timestamp;

/**
 * The type Success response body.
 */
public class SuccessResponseBody {

    /**
     * The Timestamp.
     */
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    /**
     * The Message.
     */
    private String message;

    /**
     * Instantiates a new Success response body.
     *
     * @param message the message
     */
    public SuccessResponseBody(String message) {
        this.message = message;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SuccessResponseBody{"
                + "timestamp=" + timestamp
                + ", message='" + message + '\''
                + '}';
    }
}
