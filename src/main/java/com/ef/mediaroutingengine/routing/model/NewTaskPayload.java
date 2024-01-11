package com.ef.mediaroutingengine.routing.model;

import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskMedia;

/**
 * The type New task payload.
 */
public record NewTaskPayload(Task task, TaskMedia media) {
}
