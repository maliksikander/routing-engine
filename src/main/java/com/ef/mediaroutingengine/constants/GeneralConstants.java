package com.ef.mediaroutingengine.constants;

import com.ef.mediaroutingengine.model.AgentPresence;

public class GeneralConstants {
    private GeneralConstants() {

    }

    private static AgentPresence agentPresence;
    private  static String agentPresenceKey;

    public static AgentPresence getAgentPresence() {
        return agentPresence;
    }

    public static void setAgentPresence(AgentPresence agentPresence1) {
        agentPresence = agentPresence1;
    }

    public static String getAgentPresenceKey() {
        return agentPresenceKey;
    }

    public static void setAgentPresenceKey(String agentPresenceKey) {
        GeneralConstants.agentPresenceKey = agentPresenceKey;
    }
}
