package com.ef.mediaroutingengine.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private static final String MESSAGE_KEY = "Service A";

    @Override
    public Health health() {

        if (Boolean.FALSE.equals(isRunningServiceA())) {
            return Health.down().withDetail(MESSAGE_KEY, "Not Available").build();
        }
        return Health.up().withDetail(MESSAGE_KEY, "Available").build();
    }

    private Boolean isRunningServiceA() {
        // Logic Skipped
        return true;
    }
}
