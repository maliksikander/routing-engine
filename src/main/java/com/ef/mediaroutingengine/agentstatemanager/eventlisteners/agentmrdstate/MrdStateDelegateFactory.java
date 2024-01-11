package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Mrd state delegate factory.
 */
@Service
public class MrdStateDelegateFactory {
    private final MrdPool mrdPool;

    @Autowired
    public MrdStateDelegateFactory(MrdPool mrdPool) {
        this.mrdPool = mrdPool;
    }

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
            case NOT_READY -> new MrdStateNotReady(this.mrdPool);
            case READY -> new MrdStateReady();
            case ACTIVE -> new MrdStateActive();
            case BUSY -> new MrdStateBusy();
            default -> null;
        };

    }
}
