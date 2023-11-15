package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentstate;

import com.ef.cim.objectmodel.Enums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The type Agent state delegate factory.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AgentStateDelegateFactory {
    /**
     * The Agent state not ready.
     */
    private final AgentStateNotReady agentStateNotReady;
    /**
     * The Agent state ready.
     */
    private final AgentStateReady agentStateReady;
    /**
     * The Agent state login.
     */
    private final AgentStateLogin agentStateLogin;
    /**
     * The Agent state logout.
     */
    private final AgentStateLogout agentStateLogout;

    /**
     * Default constructor. Loads the dependencies.
     *
     * @param agentStateNotReady handles Not-Ready state.
     * @param agentStateReady    handles Ready state.
     * @param agentStateLogin    handles Login state.
     * @param agentStateLogout   handles Logout state.
     */
    @Autowired
    public AgentStateDelegateFactory(AgentStateNotReady agentStateNotReady, AgentStateReady agentStateReady,
                                     AgentStateLogin agentStateLogin, AgentStateLogout agentStateLogout) {
        this.agentStateNotReady = agentStateNotReady;
        this.agentStateReady = agentStateReady;
        this.agentStateLogin = agentStateLogin;
        this.agentStateLogout = agentStateLogout;
    }

    /**
     * Returns the Agent state delegate object wrt the requested state change.
     *
     * @param state the requested Agent state to be changed
     * @return Agent state delegate for the requested state, null if requested state is null
     */
    public AgentStateDelegate getDelegate(Enums.AgentStateName state) {
        if (state == null) {
            return null;
        }
        switch (state) {
            case LOGIN:
                return agentStateLogin;
            case NOT_READY:
                return agentStateNotReady;
            case READY:
                return agentStateReady;
            case LOGOUT:
                return agentStateLogout;
            default:
                throw new IllegalArgumentException();
        }
    }
}
