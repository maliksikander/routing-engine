package com.ef.mediaroutingengine.controllers;

import java.sql.Timestamp;

public class SuccessResponseBody {

    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private String message;

    public SuccessResponseBody(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SuccessResponseBody{" +
                "timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
