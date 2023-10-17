package com.ef.mediaroutingengine.routing.utility;

import com.ef.cim.objectmodel.task.TaskMedia;
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
     * @param media the media
     * @return the offer to agent
     */
    public static boolean getOfferToAgent(TaskMedia media) {
        Map<String, Object> metadata = media.getType().getMetadata();
        if (metadata == null || metadata.get("offerToAgent") == null) {
            return true;
        }
        return (boolean) metadata.get("offerToAgent");
    }
}
