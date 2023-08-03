package com.ef.mediaroutingengine.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Queue DTO.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueDto {
    private String queueId;
    private String queueName;
}
