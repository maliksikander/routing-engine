package com.ef.mediaroutingengine.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties for Redis Config.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    private String host;
    private String password;
    private int port;
    private boolean ssl;
    private int timeout;

    private Integer maxActive;
    private Integer maxIdle;
    private Integer minIdle;
    private Integer maxWait;

    @Value("$spring.redis.enableSentinel")
    private String enableSentinel;

    @Value("$spring.redis.sentinelMaster")
    private String sentinelMaster;

    @Value("$spring.redis.sentinelNodes")
    private String sentinelNodes;

    @Value("$spring.redis.sentinelPassword")
    private String sentinelPassword;

    /**
     * Gets host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }


    /**
     * Sets host.
     *
     * @param host the host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets port.
     *
     * @param port the port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Is ssl boolean.
     *
     * @return the boolean
     */
    public boolean isSsl() {
        return ssl;
    }

    /**
     * Sets ssl.
     *
     * @param ssl the ssl
     */
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }


    /**
     * Gets max active.
     *
     * @return the max active
     */
    public Integer getMaxActive() {
        return maxActive;
    }

    /**
     * Sets max active.
     *
     * @param maxActive the max active
     */
    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    /**
     * Gets max idle.
     *
     * @return the max idle
     */
    public Integer getMaxIdle() {
        return maxIdle;
    }

    /**
     * Sets max idle.
     *
     * @param maxIdle the max idle
     */
    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    /**
     * Gets min idle.
     *
     * @return the min idle
     */
    public Integer getMinIdle() {
        return minIdle;
    }

    /**
     * Sets min idle.
     *
     * @param minIdle the min idle
     */
    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    /**
     * Gets max wait.
     *
     * @return the max wait
     */
    public Integer getMaxWait() {
        return maxWait;
    }

    /**
     * Sets max wait.
     *
     * @param maxWait the max wait
     */
    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public String getEnableSentinel() {
        return enableSentinel;
    }

    public void setEnableSentinel(String enableSentinel) {
        this.enableSentinel = enableSentinel;
    }

    public String getSentinelMaster() {
        return sentinelMaster;
    }

    public void setSentinelMaster(String sentinelMaster) {
        this.sentinelMaster = sentinelMaster;
    }

    public String getSentinelNodes() {
        return sentinelNodes;
    }

    public void setSentinelNodes(String sentinelNodes) {
        this.sentinelNodes = sentinelNodes;
    }

    public String getSentinelPassword() {
        return sentinelPassword;
    }

    public void setSentinelPassword(String sentinelPassword) {
        this.sentinelPassword = sentinelPassword;
    }


    @Override
    public String toString() {
        return "RedisProperties{"
                + "host='" + host + '\''
                + ", password='" + password + '\''
                + ", port=" + port
                + ", ssl=" + ssl
                + ", timeout=" + timeout
                + ", maxActive=" + maxActive
                + ", maxIdle=" + maxIdle
                + ", minIdle=" + minIdle
                + ", maxWait=" + maxWait
                + ", enableSentinel=" + enableSentinel
                + ", sentinelMaster='" + sentinelMaster + '\''
                + ", sentinelNodes='" + sentinelNodes + '\''
                + ", sentinelPassword='" + sentinelPassword + '\''
                + '}';
    }
}
