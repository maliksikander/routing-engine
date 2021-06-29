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

@RestController
public class MockTaskStateChanged {
    private final JmsCommunicator jmsCommunicator;

    @Autowired
    public MockTaskStateChanged(JmsCommunicator jmsCommunicator) {
        this.jmsCommunicator = jmsCommunicator;
    }

    @GetMapping("/task-closed")
    public ResponseEntity<Object> taskClosed(@RequestParam UUID taskId)
            throws JMSException, JsonProcessingException {
        this.publishTaskState(taskId, new TaskState(Enums.TaskStateName.CLOSED, null));
        return new ResponseEntity<>("Task closed", HttpStatus.OK);
    }

    @GetMapping("/task-closed-with-rona")
    public ResponseEntity<Object> taskClosedWithRona(@RequestParam UUID taskId)
            throws JMSException, JsonProcessingException {
        this.publishTaskState(taskId, new TaskState(Enums.TaskStateName.CLOSED, Enums.TaskStateReasonCode.RONA));
        return new ResponseEntity<>("Task closed", HttpStatus.OK);
    }

    @GetMapping("/task-active")
    public ResponseEntity<Object> taskActive(@RequestParam UUID taskId)
            throws JMSException, JsonProcessingException {
        this.publishTaskState(taskId, new TaskState(Enums.TaskStateName.ACTIVE, null));
        return new ResponseEntity<>("Task Active", HttpStatus.OK);
    }

    private void publishTaskState(UUID taskId, TaskState state) throws JMSException, JsonProcessingException {
        TaskStateChangeRequest data = new TaskStateChangeRequest(taskId, state);
        this.jmsCommunicator.publish(data, Enums.RedisEventName.TASK_STATE_CHANGED);
    }
}
