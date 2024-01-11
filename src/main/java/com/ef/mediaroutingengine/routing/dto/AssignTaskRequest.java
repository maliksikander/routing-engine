package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.task.TaskMedia;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Assign task request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AssignTaskRequest {
    private String taskId;
    private String conversationId;
    private TaskMedia media;
    private CCUser ccUser;
}
