package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.StateChangeEvent;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.fasterxml.jackson.core.JsonProcessingException;
import javax.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock jms publisher.
 */
@RestController
public class MockJmsPublisher {
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Mock jms publisher.
     *
     * @param jmsCommunicator the jms communicator
     */
    @Autowired
    public MockJmsPublisher(JmsCommunicator jmsCommunicator) {
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Mock the JMS publish feature for testing.
     *
     * @return response entity object
     * @throws JMSException            if exception while publishing.
     * @throws JsonProcessingException if exception while converting object to json string.
     */
    @GetMapping("/jms-publish")
    public ResponseEntity<Object> jmsPublish() throws JMSException, JsonProcessingException {
        Enums.JmsEventName event = Enums.JmsEventName.AGENT_STATE_CHANGED;
        StateChangeEvent stateChangeEvent = new StateChangeEvent(event, "state", "STATE_CHANNEL");
        this.jmsCommunicator.publish(stateChangeEvent, event);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}