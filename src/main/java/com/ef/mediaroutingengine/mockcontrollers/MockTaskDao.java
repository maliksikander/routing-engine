package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.mediaroutingengine.dto.TaskDto;
import com.ef.mediaroutingengine.model.Enums;
import com.ef.mediaroutingengine.model.TaskState;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockTaskDao {
    @Autowired
    private TasksRepository tasksRepository;

    /**
     * Mocks the Task DAO add functionality.
     *
     * @param id the id of task to be added.
     * @return response entity
     */
    @GetMapping("task-dao/add")
    public ResponseEntity<Object> add(@RequestParam String id) {
        TaskDto taskDto = new TaskDto();
        taskDto.setState(new TaskState(Enums.TaskStateName.CREATED, Enums.TaskStateReasonCode.NONE));
        taskDto.setPriority(10);
        this.tasksRepository.save(id, taskDto);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @GetMapping("task-dao/delete")
    public ResponseEntity<Object> delete(@RequestParam String id) {
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
