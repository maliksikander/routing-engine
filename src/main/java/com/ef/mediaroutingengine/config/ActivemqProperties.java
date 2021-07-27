package com.ef.mediaroutingengine.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.jms.Connection;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class reads the Activemq properties from the properties file and creates a singleton AMQ connection
 * bean. All jms communication then happens on this connection. If the connection bean fails to make a
 * connection with the AMQ client, it will through an exception and the routing-engine will not boot up.
 */
@Configuration
@ConfigurationProperties(prefix = "amq")
public class ActivemqProperties {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivemqProperties.class);
    /**
     * Client-id for the Active-mq connection. Must be unique. If an activemq connection with the same
     * client-id is already present, the connection will fail unless this field is changed.
     */
    private static final String CLIENT_ID = "ROUTING-ENGINE-CLIENT";
    /**
     * AMQ Property: transport of the broker url (tcp or ssl).
     */
    private String transport;
    /**
     * AMQ property: to make connection with the AMQ client.
     */
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;
    /**
     * AMQ property: timeout on send operations (in ms) without interruption of re-connection process.
     */
    private String timeout;
    /**
     * AMQ property: if true, choose a URI at random from the list to use for reconnect.
     */
    private String randomize;
    /**
     * AMQ property: https://activemq.apache.org/failover-transport-reference.html.
     * See the link to read properties detail.
     */
    private String priorityBackup;
    /**
     * AMQ property: -1 for infinite retries, 0 for none, other: number of times it should try to reconnect
     * with the broker.
     */
    private String maxReconnectAttempts;
    /**
     * AMQ property: path to the trust-store file.
     */
    private String trustStorePath;
    /**
     * AMQ property: password of the trust-store.
     */
    private String trustStorePassword;
    /**
     * AMQ property: path to the key-store file.
     */
    private String keyStorePath;
    /**
     * AMQ property: password of the key-store.
     */
    private String keyStorePassword;

    /**
     * Gets transport.
     *
     * @return the transport
     */
    public String getTransport() {
        return transport;
    }

    /**
     * Sets transport.
     *
     * @param transport the transport
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets randomize.
     *
     * @return the randomize
     */
    public String getRandomize() {
        return randomize;
    }

    /**
     * Sets randomize.
     *
     * @param randomize the randomize
     */
    public void setRandomize(String randomize) {
        this.randomize = randomize;
    }

    /**
     * Gets priority backup.
     *
     * @return the priority backup
     */
    public String getPriorityBackup() {
        return priorityBackup;
    }

    /**
     * Sets priority backup.
     *
     * @param priorityBackup the priority backup
     */
    public void setPriorityBackup(String priorityBackup) {
        this.priorityBackup = priorityBackup;
    }

    /**
     * Gets max reconnect attempts.
     *
     * @return the max reconnect attempts
     */
    public String getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    /**
     * Sets max reconnect attempts.
     *
     * @param maxReconnectAttempts the max reconnect attempts
     */
    public void setMaxReconnectAttempts(String maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    /**
     * Gets trust store path.
     *
     * @return the trust store path
     */
    public String getTrustStorePath() {
        return trustStorePath;
    }

    /**
     * Sets trust store path.
     *
     * @param trustStorePath the trust store path
     */
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * Gets trust store password.
     *
     * @return the trust store password
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Sets trust store password.
     *
     * @param trustStorePassword the trust store password
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Gets key store path.
     *
     * @return the key store path
     */
    public String getKeyStorePath() {
        return keyStorePath;
    }

    /**
     * Sets key store path.
     *
     * @param keyStorePath the key store path
     */
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    /**
     * Gets key store password.
     *
     * @return the key store password
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Sets key store password.
     *
     * @param keyStorePassword the key store password
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Gets broker url.
     *
     * @return the broker url
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }

    /**
     * Sets broker url.
     *
     * @param brokerUrl the broker url
     */
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    /**
     * Creates AMQ Connection bean on application start. This connection is used for all JMS communication.
     * If connection to AMQ fails, an exception will be thrown and the application will not start.
     *
     * @return AMQ Connection object
     * @throws Exception if there is issue in creating connection.
     */
    @Bean
    public Connection connectionFactory() throws Exception {
        this.brokerUrl += "?" + this.getOptions();

        Connection connection;
        if (this.getTransport().equalsIgnoreCase("ssl")) {
            connection = this.createSslConnection();
        } else {
            connection = this.createOpenWireConnection();
        }
        LOGGER.debug("Connection object successfully created with protocol type: {}", this.transport);
        connection.setClientID(CLIENT_ID);
        LOGGER.debug("Client-id for AMQ connection set successfully");
        connection.start();
        LOGGER.info("AMQ Connection on {} protocol started successfully", this.transport);
        return connection;
    }

    /**
     * Returns the AMQ broker-url options string which is made from multiple AMQ properties.
     *
     * @return the AMQ broker-url options string.
     */
    private String getOptions() {
        return "timeout=" + this.getTimeout()
                + "&randomize=" + this.getRandomize()
                + "&priorityBackup=" + this.getPriorityBackup()
                + "&maxReconnectAttempts=" + this.getMaxReconnectAttempts();
    }

    /**
     * Creates and returns a JMS-Connection object for SSL protocol.
     *
     * @return JMS -Connection object with protocol type: SSL
     * @throws Exception if there is an issue with creating absolute paths for trustStore or keyStore, or their                   is an issue with creating the Connection object from the ConnectionFactory
     */
    private Connection createSslConnection() throws Exception {
        LOGGER.debug("Creating Connection object for SSL protocol");
        ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory(this.brokerUrl);
        // Convert the relative paths in the ActivemqProperties to absolute paths
        Path trustStoreAbsolutePath = Paths.get(this.getTrustStorePath()).toAbsolutePath();
        LOGGER.debug("Absolute path for trust-store created successfully");
        connectionFactory.setTrustStore(trustStoreAbsolutePath.toString());
        connectionFactory.setTrustStorePassword(this.getTrustStorePassword());
        LOGGER.debug("Trust-store path and password set in ConnectionFactory");

        Path keyStoreAbsolutePath = Paths.get(this.getKeyStorePath()).toAbsolutePath();
        LOGGER.debug("Absolute path for key-store created successfully");
        connectionFactory.setKeyStore(keyStoreAbsolutePath.toString());
        connectionFactory.setKeyStorePassword(this.getKeyStorePassword());
        LOGGER.debug("Key-store path and password set in ConnectionFactory");

        connectionFactory.setTrustAllPackages(true);
        return connectionFactory.createConnection();
    }

    /**
     * Creates and returns a JMS-Connection object for TCP protocol.
     *
     * @return JMS -Connection object with protocol type: TCP
     * @throws JMSException if there is an issue with creating the Connection object from the ConnectionFactory
     */
    private Connection createOpenWireConnection() throws JMSException {
        LOGGER.debug("Creating Connection object for TCP protocol");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setTrustAllPackages(true);
        return connectionFactory.createConnection();
    }


    @Override
    public String toString() {
        return "ActivemqProperties{"
                + "transport='" + transport + '\''
                + ", brokerUrl='" + brokerUrl + '\''
                + ", timeout='" + timeout + '\''
                + ", randomize='" + randomize + '\''
                + ", priorityBackup='" + priorityBackup + '\''
                + ", maxReconnectAttempts='" + maxReconnectAttempts + '\''
                + ", trustStorePath='" + trustStorePath + '\''
                + ", trustStorePassword='" + trustStorePassword + '\''
                + ", keyStorePath='" + keyStorePath + '\''
                + ", keyStorePassword='" + keyStorePassword + '\''
                + '}';
    }
}
