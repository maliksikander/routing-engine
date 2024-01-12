package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.task.Task;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Mrd delete conflict response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MrdDeleteConflictResponse {
    /**
     * The Precision queues.
     */
    private List<PrecisionQueueEntity> precisionQueues;
    /**
     * The Tasks.
     */
    private List<Task> tasks;
}
