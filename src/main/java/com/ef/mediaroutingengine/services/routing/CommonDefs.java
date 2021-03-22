//package com.ef.mediaroutingengine.services.routing;
//
//public class CommonDefs {
//    public static String MRE_VERSION = "3.10.0";
//
//    private static boolean MRE_STATUS = false;
//
//    public static void setMreStatus(boolean status) {
//        MRE_STATUS = status;
//    }
//
//    public static boolean getMreStatus() {
//        return MRE_STATUS;
//    }
//
//    // TODO Move all public enums to this class
//
//    public enum AGENT_STATE {
//        LOGIN,
//        NOT_READY,
//        READY,
//        INTERRUPTED,
//        ACTIVE,
//        WORK_READY,
//        RESERVED,
//        BUSY,
//        LOGOUT,
//        WORK_NOT_READY,
//        UNKNOWN
//    }
//
//    public enum TASK_STATE {
//        NEW,
//        OFFERED,
//        ACCEPTED,
//        ACTIVE,
//        PAUSED,
//        WRAPPING_UP,
//        INTERRUPTED,
//        CLOSED,
//        UNKNOWN
//    }
//
//    public enum TERM_OPERATOR {
//        EQUAL,
//        EQUAL_OR_GRATER,
//        EQUAL_OR_LESS,
//        GRATER,
//        LESS,
//        NOT_EQUAL
//    }
//
//    public enum EXPRESSION_OPERATOR {
//        AND,
//        OR
//    }
//
//    public enum ATTRIBUTE_TYPE {
//        PROFICIENCY,
//        BOOLEAN
//    }
//
//    public enum EVENT_PROPERTIES {
//        NewTask,
//        AgentState,
//        EwtRequestEvent,
//        Timer,
//        Transfer,
//        TaskState,
//        Conference,
//        LeaveConversation,
//        NewAttribute,
//        UpdateAttribute,
//        DeleteAttribute,
//        NewAgent,
//        UpdateAgent,
//        DeleteAgent,
//        NewMRD,
//        UpdateMRD,
//        DeleteMRD,
//        NewPrecisionQueue,
//        UpdatePrecisionQueue,
//        DeletePrecisionQueue,
//        NewLabel,
//        UpdateLabel,
//        DeleteLabel,
//        AllAttributes,
//        AllAgents,
//        AllMrds,
//        AllPrecisionQueues,
//        AllLabels
//    }
//
//    public enum COMMAND_PROPERTIES {
//        DispatchSelectedAgent,
//        GetAgentState,
//        DispatchEWT
//    }
//
//    public enum CommunicationChannels {
//        ACTIVEMQ
//    }
//
//    public enum INCOMING_MSG_TYPE {
//        Newtask,
//        AgentState,
//        EWT,
//        Transfer,
//        TaskState,
//        Conference,
//        LeaveConversation,
//        NewAttribute,
//        UpdateAttribute,
//        DeleteAttribute,
//        NewAgent,
//        UpdateAgent,
//        DeleteAgent,
//        NewMRD,
//        UpdateMRD,
//        DeleteMRD,
//        NewPrecisionQueue,
//        UpdatePrecisionQueue,
//        DeletePrecisionQueue,
//        NewLabel,
//        DeleteLabel,
//        UpdateLabel,
//        AllAttributes,
//        AllAgents,
//        AllMrds,
//        AllPrecisionQueues,
//        AllLabels
//    }
//
//    public enum AMQ_QUEUE_NAME {
//        CommunicationServer,
//        MreMicroservice,
//        reporting
//    }
//
//    public enum MRE_MICROSERVICE_COMMAND {
//        GetAllAttributes,
//        GetAllAgents,
//        GetAllMrds,
//        GetAllPrecisionQueues
//    }
//
//    public enum DEFAULT_QUEUE {
//        DefaultPrecisionQueue,
//        chat
//    }
//
//    public enum AGENT_MODE {
//        ROUTABLE,
//        NON_ROUTEABLE
//    }
//}
