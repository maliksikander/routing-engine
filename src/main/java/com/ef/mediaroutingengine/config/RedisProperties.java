package com.ef.mediaroutingengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class reads the Redis properties from the application's properties file. A singleton bean of this class
 * is created at startup. This bean is used whenever these properties are required in the project.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {
    /**
     * Host on which the Redis Instance is running.
     */
    private String host;
    /**
     * Redis host password if it is password protected.
     */
    private String password;
    /**
     * Port on which the Redis instance is running.
     */
    private int port;
    /**
     * True if the transport protocol is SSL.
     */
    private boolean ssl;
    /**
     * Connection timeout (in ms).
     */
    private int timeout;
    /**
     * Maximum number of active connections in the connection pool (negative value means no limit).
     */
    private Integer maxActive;
    /**
     * Maximum number of free connections in the connection pool.
     */
    private Integer maxIdle;
    /**
     * Minimum number of free connections in the connection pool.
     */
    private Integer minIdle;
    /**
     * Connection pool maximum blocking wait time (negative value means no limit).
     */
    private Integer maxWait;

    /**
     * Name of pub-sub channel between routing-engine and Agent-manager.
     */
    private String channelTopic;


    private Sentinel sentinel;

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

    /**
     * Gets channel topic.
     *
     * @return the channel topic
     */
    public String getChannelTopic() {
        return channelTopic;
    }

    /**
     * Sets channel topic.
     *
     * @param channelTopic the channel topic
     */
    public void setChannelTopic(String channelTopic) {
        this.channelTopic = channelTopic;
    }

    public Sentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
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
                + ", channelTopic='" + channelTopic + '\''
                + '}';
    }
}
