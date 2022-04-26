package com.ef.mediaroutingengine.services.controllerservices;

import com.ef.mediaroutingengine.dto.QueueStatsDto;
import java.util.List;

/**
 * The interface Queue stats service.
 */
public interface QueueStatsService {
    /**
     * Gets queue stats for.
     *
     * @param queueId the queue id
     * @return the queue stats for
     */
    QueueStatsDto getQueueStats(String queueId);

    /**
     * Gets queue stats for all.
     *
     * @return the queue stats for all
     */
    List<QueueStatsDto> getQueueStatsForAll();
}
