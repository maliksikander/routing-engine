package com.ef.mediaroutingengine.model;

public class Enums {
    private static boolean mreStatus = false;

    public static void setMreStatus(boolean status) {
        mreStatus = status;
    }

    public static boolean getMreStatus() {
        return mreStatus;
    }

    public enum TaskStateName {
        CREATED,
        QUEUED,
        RESERVED,
        ACTIVE,
        PAUSED,
        WRAP_UP,
        CLOSED
    }

    public enum EventName {
        NEW_TASK,
        TASK_REMOVED,
        AGENT_STATE,
        AGENT_MRD_STATE,
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

    public enum AgentStateName {
        LOGIN,
        NOT_READY,
        READY,
        LOGOUT,
        ACTIVE
    }

    public enum AgentStateReasonCode {
        NONE
    }

    public enum AgentMrdStateName {
        READY,
        NOT_READY,
        INTERRUPTED,
        ACTIVE,
        BUSY,
        UNKNOWN
    }

    public enum AgentMrdStateReasonCode {
        NONE
    }

    public enum AgentMode {
        ROUTABLE,
        NON_ROUTABLE
    }

    public enum DefaultQueue {
        DEFAULT_PRECISION_QUEUE,
        CHAT
    }

    public enum RedisEventName {
        TASK_STATE_CHANGED,
        AGENT_STATE_CHANGED,
        AGENT_MRD_STATE_CHANGED
    }

    public enum TaskStateReasonCode {
        RONA,
        DONE,
        RESPONSE_TIMEOUT,
        NO_AGENT_AVAILABLE,
        NONE
    }
}
