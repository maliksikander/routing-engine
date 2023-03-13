package com.ef.mediaroutingengine.global.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * The type Custom health indicator.
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    /**
     * The constant MESSAGE_KEY.
     */
    private static final String MESSAGE_KEY = "Service A";

    @Override
    public Health health() {

        if (Boolean.FALSE.equals(isRunningServiceA())) {
            return Health.down().withDetail(MESSAGE_KEY, "Not Available").build();
        }
        return Health.up().withDetail(MESSAGE_KEY, "Available").build();
    }

    /**
     * Is running service A running.
     *
     * @return the boolean
     */
    private Boolean isRunningServiceA() {
        // Logic Skipped
        return true;
    }
}
