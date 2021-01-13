package com.ef.mediaroutingengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value="classpath:application.properties")
public class AssignResourceConfiguration {
    @Value("${assign_resource_retries}")
    private String assignResourceRetries;

    @Value("${change_state_uri}")
    private String changeStateUri;

    @Value("${agent_reserved_uri}")
    private String agentReservedUri;

    @Value("${agent_ewt_Uri}")
    private String agentEwtUri;

    @Value("${no_agent_available_uri}")
    private String noAgentAvailableUri;

    @Value("${assign_task_uri}")
    private String assignTaskUri;

    public int getAssignResourceRetries() {
        return Integer.parseInt(assignResourceRetries);
    }

    public void setAssignResourceRetries(int assignResourceRetries) {
        this.assignResourceRetries = String.valueOf(assignResourceRetries);
    }

    public String getChangeStateUri() {
        return changeStateUri;
    }

    public void setChangeStateUri(String changeStateUri) {
        this.changeStateUri = changeStateUri;
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

    public String getAssignTaskUri() {
        return assignTaskUri;
    }

    public void setAssignTaskUri(String assignTaskUri) {
        this.assignTaskUri = assignTaskUri;
    }
}
