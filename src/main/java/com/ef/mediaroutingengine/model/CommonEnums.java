package com.ef.mediaroutingengine.model;

public class CommonEnums {
    private static boolean mreStatus = false;

    public static void setMreStatus(boolean status) {
        mreStatus = status;
    }

    public static boolean getMreStatus() {
        return mreStatus;
    }

    public enum TaskState {
        NEW,
        OFFERED,
        ACCEPTED,
        ACTIVE,
        PAUSED,
        WRAPPING_UP,
        INTERRUPTED,
        CLOSED,
        UNKNOWN
    }

    public enum EventProperties {
        NEW_TASK,
        AGENT_STATE,
        EWT_REQUEST_EVENT,
        TIMER,
        TRANSFER,
        TASK_STATE,
        CONFERENCE,
        LEAVE_CONVERSATION,
        NEW_ATTRIBUTE,
        UPDATE_ATTRIBUTE,
        DELETE_ATTRIBUTE,
        NEW_AGENT,
        UPDATE_AGENT,
        DELETE_AGENT,
        NEW_MRD,
        UPDATE_MRD,
        DELETE_MRD,
        NEW_PRECISION_QUEUE,
        UPDATE_PRECISION_QUEUE,
        DELETE_PRECISION_QUEUE,
        NEW_LABEL,
        UPDATE_LABEL,
        DELETE_LABEL,
        ALL_ATTRIBUTES,
        ALL_AGENTS,
        ALL_MRDS,
        ALL_PRECISION_QUEUES,
        ALL_LABELS
    }

    public enum CommandProperties {
        DISPATCH_SELECTED_AGENT,
        GET_AGENT_STATE,
        DISPATCH_EWT
    }

    public enum AgentState {
        LOGIN,
        NOT_READY,
        READY,
        INTERRUPTED,
        ACTIVE,
        WORK_READY,
        RESERVED,
        BUSY,
        LOGOUT,
        WORK_NOT_READY,
        UNKNOWN
    }

    public enum AgentMode {
        ROUTABLE,
        NON_ROUTABLE
    }

    public enum DefaultQueue {
        DEFAULT_PRECISION_QUEUE,
        CHAT
    }

    public enum IncomingMsgType {
        NEW_TASK,
        AGENT_STATE,
        EWT,
        TRANSFER,
        TASK_STATE,
        CONFERENCE,
        LEAVE_CONVERSATION,
        NEW_ATTRIBUTE,
        UPDATE_ATTRIBUTE,
        DELETE_ATTRIBUTE,
        NEW_AGENT,
        UPDATE_AGENT,
        DELETE_AGENT,
        NEW_MRD,
        UPDATE_MRD,
        DELETE_MRD,
        NEW_PRECISION_QUEUE,
        UPDATE_PRECISION_QUEUE,
        DELETE_PRECISION_QUEUE,
        NEW_LABEL,
        DELETE_LABEL,
        UPDATE_LABEL,
        ALL_ATTRIBUTES,
        ALL_AGENTS,
        ALL_MRDS,
        ALL_PRECISION_QUEUES,
        ALL_LABELS
    }

    public enum RedisEventName {
        TASK_STATE_CHANGED,
        AGENT_MRD_STATE_CHANGED
    }
}
