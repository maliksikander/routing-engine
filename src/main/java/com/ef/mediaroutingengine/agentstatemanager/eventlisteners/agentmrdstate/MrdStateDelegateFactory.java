package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.Enums;
import org.springframework.stereotype.Service;

/**
 * The type Mrd state delegate factory.
 */
@Service
public class MrdStateDelegateFactory {

    /**
     * Returns the MRD state Delegate wrt the requested state to be changed.
     *
     * @param state the requested Agent MRD state name.
     * @return MRD State Delegate.
     */
    public MrdStateDelegate getDelegate(Enums.AgentMrdStateName state) {
        if (state == null) {
            return null;
        }

        switch (state) {
            case NOT_READY:
                return new MrdStateNotReady();
            case READY:
                return new MrdStateReady();
            case ACTIVE:
                return new MrdStateActive();
            case BUSY:
                return new MrdStateBusy();
            default:
                return null;
        }
    }
}
