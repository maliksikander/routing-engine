package com.ef.mediaroutingengine.taskmanager.dto;

import com.ef.cim.objectmodel.task.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The ewt and position type response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskEwtResponse {
    private Task task;
    private int ewt;
    private int position;
}
