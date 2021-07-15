package com.ef.mediaroutingengine.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.jms.Connection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Properties for ActiveMQ configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "amq")
public class ActivemqProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivemqProperties.class);
    private static final String CLIENT_ID = "ROUTING-ENGINE-CLIENT-2";

    private String transport;
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;
    private String timeout;
    private String randomize;
    private String priorityBackup;
    private String maxReconnectAttempts;
    private String trustStorePath;
    private String trustStorePassword;
    private String keyStorePath;
    private String keyStorePassword;

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getRandomize() {
        return randomize;
    }

    public void setRandomize(String randomize) {
        this.randomize = randomize;
    }

    public String getPriorityBackup() {
        return priorityBackup;
    }

    public void setPriorityBackup(String priorityBackup) {
        this.priorityBackup = priorityBackup;
    }

    public String getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public void setMaxReconnectAttempts(String maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    /**
     * Creates AMQ Connection on application start.
     *
     * @return AMQ Connection
     * @throws Exception exception
     */
    @Bean
    public Connection connectionFactory() throws Exception {

        Connection connection;
        String options =
                "timeout="
                        + this.getTimeout()
                        + "&randomize="
                        + getRandomize()
                        + "&priorityBackup="
                        + getPriorityBackup()
                        + "&maxReconnectAttempts="
                        + getMaxReconnectAttempts();

        brokerUrl += "?" + options;
        if (this.getTransport().equalsIgnoreCase("ssl")) {
            LOGGER.debug("ActivemqServiceImpl.setConnection | Connection type: SSL");
            ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory(brokerUrl);
            // Convert the relative paths in the ActivemqProperties to absolute paths
            Path trustStorePath = Paths.get(this.getTrustStorePath()).toAbsolutePath();
            Path keyStorePath = Paths.get(this.getKeyStorePath()).toAbsolutePath();

            connectionFactory.setTrustStore(trustStorePath.toString());
            connectionFactory.setTrustStorePassword(this.getTrustStorePassword());
            connectionFactory.setKeyStore(keyStorePath.toString());
            connectionFactory.setKeyStorePassword(this.getKeyStorePassword());

            connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createConnection();

        } else {
            LOGGER.debug("ActivemqServiceImpl.setConnection | Connection type: open wire");
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createConnection();
        }
        connection.setClientID(CLIENT_ID);
        LOGGER.debug("ActivemqServiceImpl.setConnection | Connection created successfully");

        connection.start();
        LOGGER.debug("ActivemqServiceImpl.setConnection | Connection started successfully");
        return connection;
    }


    @Override
    public String toString() {
        return "ActivemqProperties{"
                + "protocol="
                + transport
                + ", brokerUrl='"
                + brokerUrl
                + '\''
                + ", timeout='"
                + timeout
                + '\''
                + ", randomize='"
                + randomize
                + '\''
                + ", priorityBackup='"
                + priorityBackup
                + '\''
                + ", maxReconnectAttempts='"
                + maxReconnectAttempts
                + '\''
                + ", trustStorePath='"
                + trustStorePath
                + '\''
                + ", trustStorePassword='"
                + trustStorePassword
                + '\''
                + ", keyStorePath='"
                + keyStorePath
                + '\''
                + ", keyStorePassword='"
                + keyStorePassword
                + '\''
                + '}';
    }
}
