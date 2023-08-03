package com.ef.mediaroutingengine.routing.dto;

import lombok.Data;

/**
 * The QueueHistoricalStats DTO.
 */
@Data
public class QueueHistoricalStats {
    private QueueDto queue;
    private int averageWaitTime;
    private int averageHandleTime;
}
