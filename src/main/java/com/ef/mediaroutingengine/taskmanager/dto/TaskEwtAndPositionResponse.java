package com.ef.mediaroutingengine.taskmanager.dto;

import com.ef.mediaroutingengine.taskmanager.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The ewt and position type response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskEwtAndPositionResponse {
    private Task task;
    private int ewt;
    private int position;
}
