package com.ef.mediaroutingengine.config.jmsconfig;

import javax.jms.Connection;

/**
 * The interface Jms config implementation.
 */
public interface JmsConfig {
    /**
     * Gets connection.
     *
     * @return the connection
     * @throws Exception the exception
     */
    Connection getConnection() throws Exception;

    /**
     * Gets transport.
     *
     * @return the transport
     */
    String getTransport();
}
