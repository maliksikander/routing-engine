package com.ef.mediaroutingengine.config.jmsconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the external components URI(s).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "external.service")
public class ExternalServiceConfig {

    private String realTimeReportsUri;

}
