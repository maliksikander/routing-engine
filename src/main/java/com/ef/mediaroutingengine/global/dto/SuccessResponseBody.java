package com.ef.mediaroutingengine.global.dto;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Success response body.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
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
}
