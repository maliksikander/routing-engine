package com.ef.mediaroutingengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "routing-engine.assign-resource")
public class AssignResourceProperties {
    private int retries;
    private String changeStateUri;
    private String assignTaskUri;
    private String agentReservedUri;
    private String agentEwtUri;
    private String noAgentAvailableUri;

    public int getRetries() {
        return this.retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getChangeStateUri() {
        return changeStateUri;
    }

    public void setChangeStateUri(String changeStateUri) {
        this.changeStateUri = changeStateUri;
    }

    public String getAssignTaskUri() {
        return assignTaskUri;
    }

    public void setAssignTaskUri(String assignTaskUri) {
        this.assignTaskUri = assignTaskUri;
    }

    public String getAgentReservedUri() {
        return agentReservedUri;
    }

    public void setAgentReservedUri(String agentReservedUri) {
        this.agentReservedUri = agentReservedUri;
    }

    public String getAgentEwtUri() {
        return agentEwtUri;
    }

    public void setAgentEwtUri(String agentEwtUri) {
        this.agentEwtUri = agentEwtUri;
    }

    public String getNoAgentAvailableUri() {
        return noAgentAvailableUri;
    }

    public void setNoAgentAvailableUri(String noAgentAvailableUri) {
        this.noAgentAvailableUri = noAgentAvailableUri;
    }
}
