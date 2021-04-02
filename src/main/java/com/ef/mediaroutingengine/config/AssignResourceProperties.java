package com.ef.mediaroutingengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "routing-engine.assign-resource")
public class AssignResourceProperties {
    private int retries;
    private String agentManagerBaseUri;
    private String botFrameworkBaseUri;

    public int getRetries() {
        return this.retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getAgentManagerBaseUri() {
        return agentManagerBaseUri;
    }

    public void setAgentManagerBaseUri(String agentManagerBaseUri) {
        this.agentManagerBaseUri = agentManagerBaseUri;
    }

    public String getBotFrameworkBaseUri() {
        return botFrameworkBaseUri;
    }

    public void setBotFrameworkBaseUri(String botFrameworkBaseUri) {
        this.botFrameworkBaseUri = botFrameworkBaseUri;
    }

    public String getChangeStateUri() {
        return this.getAgentManagerBaseUri() + "/api/v1/agent/state";
    }

    public String getAssignTaskUri() {
        return this.getAgentManagerBaseUri() + "/api/v1/agent/task";
    }

    public String getAgentReservedUri() {
        return this.getBotFrameworkBaseUri() + "/agent-reserved";
    }

    public String getAgentEwtUri() {
        return this.getBotFrameworkBaseUri() + "/ewt";
    }

    public String getNoAgentAvailableUri() {
        return this.getBotFrameworkBaseUri() + "/no-agent-available";
    }
}
