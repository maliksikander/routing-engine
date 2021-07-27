package com.ef.mediaroutingengine.exceptions;

import java.sql.Timestamp;

/**
 * The type Error response body.
 */
public class ErrorResponseBody {

    /**
     * The Timestamp.
     */
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    /**
     * The Error.
     */
    private String error;
    /**
     * The Message.
     */
    private String message;

    /**
     * Instantiates a new Error response body.
     *
     * @param error   the error
     * @param message the message
     */
    public ErrorResponseBody(String error, String message) {
        this.error = error;
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
     * Gets error.
     *
     * @return the error
     */
    public String getError() {
        return error;
    }

    /**
     * Sets error.
     *
     * @param error the error
     */
    public void setError(String error) {
        this.error = error;
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
        return "ErrorResponseBody{"
                + "timestamp=" + timestamp
                + ", error='" + error + '\''
                + ", message='" + message + '\''
                + '}';
    }
}
