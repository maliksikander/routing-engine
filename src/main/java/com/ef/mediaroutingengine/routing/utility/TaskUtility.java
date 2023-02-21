package com.ef.mediaroutingengine.routing.utility;

import com.ef.mediaroutingengine.taskmanager.model.Task;
import java.util.Map;

/**
 * The type Task utility.
 */
public final class TaskUtility {
    /**
     * Instantiates a new Task utility.
     */
    private TaskUtility() {

    }

    /**
     * Gets offer to agent.
     *
     * @param task the task
     * @return the offer to agent
     */
    public static boolean getOfferToAgent(Task task) {
        Map<String, Object> metadata = task.getType().getMetadata();
        if (metadata == null || metadata.get("offerToAgent") == null) {
            return true;
        }
        return (boolean) metadata.get("offerToAgent");
    }
}
