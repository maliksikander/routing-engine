package com.ef.mediaroutingengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class reads the Assign-Resource feature properties from the application's property file. A singleton bean
 * for this class is created at startup. This bean is used in the project wherever these properties are required.
 */
@Configuration
@ConfigurationProperties(prefix = "routing-engine.assign-resource")
public class AssignResourceProperties {
    /**
     * Number of retries to assign a task to an Agent on 404 from Agent Manager.
     */
    private int retries;
    /**
     * Base url of Agent-Manager component to call it's Apis.
     */
    private String agentManagerBaseUri;
    /**
     * Base url of Bot-Framework component to call it's Apis.
     */
    private String botFrameworkBaseUri;

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
    }

    /**
     * Gets bot framework base uri.
     *
     * @return the bot framework base uri
     */
    public String getBotFrameworkBaseUri() {
        return botFrameworkBaseUri;
    }

    /**
     * Sets bot framework base uri.
     *
     * @param botFrameworkBaseUri the bot framework base uri
     */
    public void setBotFrameworkBaseUri(String botFrameworkBaseUri) {
        this.botFrameworkBaseUri = botFrameworkBaseUri;
    }

    /**
     * Returns the complete url of Agent-Manager's Assign-Task API.
     *
     * @return Agent -Manager's Assign-Task API's url
     */
    public String getAssignTaskUri() {
        return this.getAgentManagerBaseUri() + "/api/v1/agent/task";
    }

    /**
     * Returns the complete url of Bot-Framework's Agent-Reserved API.
     *
     * @return Bot -Framework's Agent-Reserved API's url
     */
    public String getAgentReservedUri() {
        return this.getBotFrameworkBaseUri() + "/agent-reserved";
    }

    /**
     * Returns the complete url of Bot-Framework's No-Agent-Available API.
     *
     * @return Bot -Framework's No-Agent-Available API's url
     */
    public String getNoAgentAvailableUri() {
        return this.getBotFrameworkBaseUri() + "/no-agent-available";
    }
}
