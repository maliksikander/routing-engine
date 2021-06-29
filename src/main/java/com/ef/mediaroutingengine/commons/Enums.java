package com.ef.mediaroutingengine.commons;

public class Enums {
    private static boolean mreStatus = false;

    public static boolean getMreStatus() {
        return mreStatus;
    }

    public static void setMreStatus(boolean status) {
        mreStatus = status;
    }

    public enum TaskStateName {
        QUEUED,
        RESERVED,
        ACTIVE,
        PAUSED,
        WRAP_UP,
        CLOSED
    }

    public enum TaskStateReasonCode {
        RONA,
        DONE,
        RESPONSE_TIMEOUT,
        NO_AGENT_AVAILABLE,
        REROUTE,
    }

    public enum EventName {
        NEW_TASK,
        TASK_REMOVED,
        AGENT_STATE,
        AGENT_MRD_STATE,
        EWT_REQUEST_EVENT,
        TIMER,
        TASK_STATE,
        CONFERENCE,
    }

    public enum CommandProperties {
        GET_AGENT_STATE,
        DISPATCH_EWT
    }

    public enum AgentStateName {
        LOGIN,
        NOT_READY,
        READY,
        LOGOUT
    }

    public enum ReasonCodeType {
        NOT_READY,
        LOGOUT
    }

    public enum AgentMrdStateName {
        LOGOUT,
        LOGIN,
        NOT_READY,
        PENDING_NOT_READY,
        READY,
        INTERRUPTED,
        ACTIVE,
        BUSY,
        UNKNOWN
    }

    public enum DefaultQueue {
        DEFAULT_PRECISION_QUEUE,
        CHAT
    }

    public enum RedisEventName {
        TASK_STATE_CHANGED,
        AGENT_STATE_CHANGED
    }
}
