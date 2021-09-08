package com.ef.mediaroutingengine.config.jmsconfig;

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
import org.springframework.context.annotation.Configuration;

/**
 * This class reads the Activemq properties from the properties file and creates a singleton AMQ connection
 * bean. All jms communication then happens on this connection. If the connection bean fails to make a
 * connection with the AMQ client, it will through an exception and the routing-engine will not boot up.
 */
@Configuration
@ConfigurationProperties(prefix = "amq")
public class ActivemqConfig implements JmsConfig {
    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ActivemqConfig.class);
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

    @Override
    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
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

    /**
     * Creates AMQ Connection bean on application start. This connection is used for all JMS communication.
     * If connection to AMQ fails, an exception will be thrown and the application will not start.
     *
     * @return AMQ Connection object
     * @throws Exception if there is issue in creating connection.
     */
    public Connection getConnection() throws Exception {
        this.brokerUrl += "?" + this.getOptions();
        Connection connection;
        if (this.transport.equalsIgnoreCase("ssl")) {
            connection = this.createSslConnection();
        } else {
            connection = this.createOpenWireConnection();
        }
        logger.debug("Connection object successfully created with protocol type: {}", this.transport);
        return connection;
    }

    /**
     * Returns the AMQ broker-url options string which is made from multiple AMQ properties.
     *
     * @return the AMQ broker-url options string.
     */
    private String getOptions() {
        return "timeout=" + this.timeout
                + "&randomize=" + this.randomize
                + "&priorityBackup=" + this.priorityBackup
                + "&maxReconnectAttempts=" + this.maxReconnectAttempts;
    }

    /**
     * Creates and returns a JMS-Connection object for SSL protocol.
     *
     * @return JMS -Connection object with protocol type: SSL
     * @throws Exception if there is an issue with creating absolute paths for trustStore or keyStore, or there
     *                   is an issue with creating the Connection object from the ConnectionFactory
     */
    private Connection createSslConnection() throws Exception {
        logger.debug("Creating Connection object for SSL protocol");
        ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory(this.brokerUrl);
        // Convert the relative paths in the ActivemqProperties to absolute paths
        Path trustStoreAbsolutePath = Paths.get(this.trustStorePath).toAbsolutePath();
        logger.debug("Absolute path for trust-store created successfully");
        connectionFactory.setTrustStore(trustStoreAbsolutePath.toString());
        connectionFactory.setTrustStorePassword(this.trustStorePassword);
        logger.debug("Trust-store path and password set in ConnectionFactory");

        Path keyStoreAbsolutePath = Paths.get(this.keyStorePath).toAbsolutePath();
        logger.debug("Absolute path for key-store created successfully");
        connectionFactory.setKeyStore(keyStoreAbsolutePath.toString());
        connectionFactory.setKeyStorePassword(this.keyStorePassword);
        logger.debug("Key-store path and password set in ConnectionFactory");

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
        logger.debug("Creating Connection object for TCP protocol");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setTrustAllPackages(true);
        return connectionFactory.createConnection();
    }
}
