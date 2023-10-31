package com.ef.mediaroutingengine.routing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Agent req timer entity.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class AgentReqTimerEntity {
    private String taskId;
    private String mediaId;
    private String queueId;
}
