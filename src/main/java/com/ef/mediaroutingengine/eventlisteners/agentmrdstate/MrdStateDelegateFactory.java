package com.ef.mediaroutingengine.eventlisteners.agentmrdstate;

import com.ef.mediaroutingengine.commons.Enums;
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
                break;
        }
        return null;
    }
}
