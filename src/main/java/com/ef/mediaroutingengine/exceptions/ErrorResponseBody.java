package com.ef.mediaroutingengine.exceptions;

import java.sql.Timestamp;

public class ErrorResponseBody {

    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private String error;
    private String message;

    public ErrorResponseBody(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorResponseBody{" +
                "timestamp=" + timestamp +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
