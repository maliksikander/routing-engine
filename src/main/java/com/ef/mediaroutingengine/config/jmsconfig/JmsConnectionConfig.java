package com.ef.mediaroutingengine.config.jmsconfig;

import javax.jms.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Jms config.
 */
@Configuration
public class JmsConnectionConfig {
    /**
     * Client-id for the JMS connection. Must be unique. If a JMS connection with the same
     * client-id is already present, the connection will fail unless this field is changed.
     */
    private static final String CLIENT_ID = "ROUTING-ENGINE-CLIENT1";
    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(JmsConnectionConfig.class);
    private final JmsConfig jmsConfig;

    @Autowired
    public JmsConnectionConfig(JmsConfig jmsConfig) {
        this.jmsConfig = jmsConfig;
    }

    /**
     * Creates JMS Connection bean on application start. This connection is used for all JMS communication.
     * If connection to JMS fails, an exception will be thrown and the application will not start.
     *
     * @return JMS Connection object
     * @throws Exception if there is issue in creating or starting connection.
     */
    @Bean
    public Connection getJmsConnection() throws Exception {
        Connection connection = jmsConfig.getConnection();
        connection.setClientID(CLIENT_ID);
        logger.debug("Client-id for JMS connection set successfully");
        connection.start();
        logger.info("JMS Connection on {} protocol started successfully", jmsConfig.getTransport());
        return connection;
    }
}
