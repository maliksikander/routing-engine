package com.ef.mediaroutingengine.mockcontrollers;

import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.TaskState;
import com.ef.cim.objectmodel.dto.TaskDto;
import com.ef.mediaroutingengine.repositories.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Mock task dao.
 */
@RestController
public class MockTaskDao {
    /**
     * The Tasks repository.
     */
    @Autowired
    private TasksRepository tasksRepository;

    /**
     * Gets all tasks.
     *
     * @return the all tasks
     */
    @GetMapping("/get-all-tasks")
    public ResponseEntity<Object> getAllTasks() {
        return new ResponseEntity<>(this.tasksRepository.findAll(), HttpStatus.OK);
    }

    /**
     * Mocks the Task DAO add functionality.
     *
     * @param id the id of task to be added.
     * @return response entity
     */
    @GetMapping("task-dao/add")
    public ResponseEntity<Object> add(@RequestParam String id) {
        TaskDto taskDto = new TaskDto();
        taskDto.setState(new TaskState(Enums.TaskStateName.QUEUED, null));
        taskDto.setPriority(10);
        this.tasksRepository.save(id, taskDto);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    /**
     * Delete response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @GetMapping("task-dao/delete")
    public ResponseEntity<Object> delete(@RequestParam String id) {
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
