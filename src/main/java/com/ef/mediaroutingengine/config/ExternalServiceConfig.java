package com.ef.mediaroutingengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class reads the External Services properties from the application's property file. A singleton bean
 * for this class is created at startup. This bean is used in the project wherever these properties are required.
 */
@Configuration
@ConfigurationProperties(prefix = "external.service")
public class ExternalServiceConfig {
    /**
     * Number of retries to assign a task to an Agent on 404 from Agent Manager.
     */
    private int retries;
    /**
     * Base url of Agent-Manager component to call its Apis.
     */
    private String agentManagerBaseUri;
    /**
     * The Assign task uri.
     */
    private String assignTaskUri;
    /**
     * The Revoke task uri.
     */
    private String revokeTaskUri;
    /**
     *  The real-time reports uri.
     */
    private String realTimeReportsUri;


    /**
     * Gets retries.
     *
     * @return the retries
     */
    public int getRetries() {
        return this.retries;
    }

    /**
     * Sets retries.
     *
     * @param retries the retries
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * Gets agent manager base uri.
     *
     * @return the agent manager base uri
     */
    public String getAgentManagerBaseUri() {
        return agentManagerBaseUri;
    }

    /**
     * Sets agent manager base uri.
     *
     * @param agentManagerBaseUri the agent manager base uri
     */
    public void setAgentManagerBaseUri(String agentManagerBaseUri) {
        this.agentManagerBaseUri = agentManagerBaseUri;
        this.assignTaskUri = agentManagerBaseUri + "/agent/assign-task";
        this.revokeTaskUri = agentManagerBaseUri + "/agent/revoke-task";
    }

    /**
     * Sets the real time reports uri.
     *
     * @param realTimeReportsUri the real-time reports uri.
     */
    public void setRealTimeReportsUri(String realTimeReportsUri) {
        this.realTimeReportsUri = realTimeReportsUri;
    }

    /**
     * Gets the real-time reports uri.
     *
     * @return the real-time reporting URI.
     */
    public String getRealTimeReportsUri() {
        return this.realTimeReportsUri;
    }

    /**
     * Returns the complete url of Agent-Manager's Assign-Task API.
     *
     * @return Agent -Manager's Assign-Task API's url
     */
    public String getAssignTaskUri() {
        return this.assignTaskUri;
    }

    /**
     * Gets revoke task uri.
     *
     * @return the revoke task uri
     */
    public String getRevokeTaskUri() {
        return this.revokeTaskUri;
    }
}
