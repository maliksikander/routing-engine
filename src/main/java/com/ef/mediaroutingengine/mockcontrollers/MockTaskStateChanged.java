package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.commons.Enums;
import com.ef.mediaroutingengine.dto.TaskStateChangeRequest;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.services.jms.JmsCommunicator;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import javax.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock task state changed.
 */
@RestController
public class MockTaskStateChanged {
    /**
     * The Jms communicator.
     */
    private final JmsCommunicator jmsCommunicator;

    /**
     * Instantiates a new Mock task state changed.
     *
     * @param jmsCommunicator the jms communicator
     */
    @Autowired
    public MockTaskStateChanged(JmsCommunicator jmsCommunicator) {
        this.jmsCommunicator = jmsCommunicator;
    }

    /**
     * Task closed response entity.
     *
     * @param taskId the task id
     * @return the response entity
     * @throws JMSException            the jms exception
     * @throws JsonProcessingException the json processing exception
     */
    @GetMapping("/task-closed")
    public ResponseEntity<Object> taskClosed(@RequestParam UUID taskId)
            throws JMSException, JsonProcessingException {
        this.publishTaskState(taskId, new TaskState(Enums.TaskStateName.CLOSED, null));
        return new ResponseEntity<>("Task closed", HttpStatus.OK);
    }

    /**
     * Task closed with rona response entity.
     *
     * @param taskId the task id
     * @return the response entity
     * @throws JMSException            the jms exception
     * @throws JsonProcessingException the json processing exception
     */
    @GetMapping("/task-closed-with-rona")
    public ResponseEntity<Object> taskClosedWithRona(@RequestParam UUID taskId)
            throws JMSException, JsonProcessingException {
        this.publishTaskState(taskId, new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.RONA));
        return new ResponseEntity<>("Task closed", HttpStatus.OK);
    }

    /**
     * Task active response entity.
     *
     * @param taskId the task id
     * @return the response entity
     * @throws JMSException            the jms exception
     * @throws JsonProcessingException the json processing exception
     */
    @GetMapping("/task-active")
    public ResponseEntity<Object> taskActive(@RequestParam UUID taskId)
            throws JMSException, JsonProcessingException {
        this.publishTaskState(taskId, new TaskState(Enums.TaskStateName.ACTIVE, null));
        return new ResponseEntity<>("Task Active", HttpStatus.OK);
    }

    /**
     * Publish task state.
     *
     * @param taskId the task id
     * @param state  the state
     * @throws JMSException            the jms exception
     * @throws JsonProcessingException the json processing exception
     */
    private void publishTaskState(UUID taskId, TaskState state) throws JMSException, JsonProcessingException {
        TaskStateChangeRequest data = new TaskStateChangeRequest(taskId, state);
        this.jmsCommunicator.publish(data, Enums.JmsEventName.TASK_STATE_CHANGED);
    }
}
