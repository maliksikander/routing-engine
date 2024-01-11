package com.ef.mediaroutingengine.routing.model;

/**
 * The type Queue event.
 */
public final class QueueEventName {
    /**
     * Instantiates a new Queue event.
     */
    private QueueEventName() {

    }

    /**
     * The constant NEW_REQUEST.
     */
    public static final String NEW_REQUEST = "NEW_REQUEST";
    /**
     * The constant REQUEST_ACCEPTED.
     */
    public static final String REQUEST_ACCEPTED = "REQUEST_ACCEPTED";
    /**
     * The constant AGENT_AVAILABLE.
     */
    public static final String AGENT_AVAILABLE = "AGENT_AVAILABLE";
    /**
     * The constant STEP_TIMEOUT.
     */
    public static final String STEP_TIMEOUT = "STEP_TIMEOUT";
    /**
     * The constant ON_FAILOVER.
     */
    public static final String ON_FAILOVER = "ON_FAILOVER";
}
