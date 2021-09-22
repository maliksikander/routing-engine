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
        if (state == null) {
            return null;
        }

        return switch (state) {
            case NOT_READY -> new MrdStateNotReady();
            case READY -> new MrdStateReady();
            case ACTIVE -> new MrdStateActive();
            case BUSY -> new MrdStateBusy();
            default -> null;
        };
    }
}
