package com.ef.mediaroutingengine.taskmanager.dto;

import com.ef.cim.objectmodel.dto.TaskDto;
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
    private TaskDto task;
    private int ewt;
    private int position;
}
