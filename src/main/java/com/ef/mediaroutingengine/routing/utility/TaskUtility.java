package com.ef.mediaroutingengine.routing.utility;

import com.ef.cim.objectmodel.ChannelSession;
import com.ef.cim.objectmodel.Enums;
import com.ef.cim.objectmodel.MrdType;
import com.ef.cim.objectmodel.task.Task;
import com.ef.cim.objectmodel.task.TaskAgent;
import com.ef.cim.objectmodel.task.TaskMedia;
import com.ef.cim.objectmodel.task.TaskState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    /**
     * Create new task task.
     *
     * @param conversationId the conversation id
     * @param media          the media
     * @param agent          the agent
     * @return the task
     */
    public static Task createNewTask(String conversationId, TaskMedia media, TaskAgent agent) {
        List<TaskMedia> medias = new ArrayList<>();
        medias.add(media);

        TaskState taskState = new TaskState(Enums.TaskStateName.ACTIVE, null);
        return new Task(media.getTaskId(), conversationId, taskState, agent, UUID.randomUUID().toString(), medias);
    }

    /**
     * Gets sessions.
     *
     * @param allSessions    the all sessions
     * @param requestSession the request session
     * @param mrdId          the mrd id
     * @param mrdType        the mrd type
     * @return the sessions
     */
    public static List<ChannelSession> getSessions(List<ChannelSession> allSessions, ChannelSession requestSession,
                                                   String mrdId, MrdType mrdType) {
        if (mrdType.isAutoJoin()) {
            return allSessions.stream()
                    .filter(c -> c.getChannel().getChannelType().getMediaRoutingDomain().equals(mrdId)).toList();
        }

        List<ChannelSession> channelSessions = new ArrayList<>();
        channelSessions.add(requestSession);
        return channelSessions;
    }
}
