package com.ef.mediaroutingengine.agentstatemanager.eventlisteners.agentmrdstate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ef.cim.objectmodel.Enums;
import com.ef.mediaroutingengine.routing.pool.MrdPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MrdStateDelegateFactoryTest {
    @Mock
    private MrdPool mrdPool;

    MrdStateDelegateFactory factory;

    @BeforeEach
    void setUp() {
        this.factory = new MrdStateDelegateFactory(this.mrdPool);
    }

    @Test
    void testGetDelegate_returnsNull_when_requestedStateIsNull() {
        assertNull(factory.getDelegate(null));
    }

    @Test
    void testGetDelegate_returnsNull_when_requestedStateIsLogout() {
        assertNull(factory.getDelegate(Enums.AgentMrdStateName.LOGOUT));
    }

    @Test
    void testGetDelegate_returnsNull_when_requestedStateIsLogin() {
        assertNull(factory.getDelegate(Enums.AgentMrdStateName.LOGIN));
    }

    @Test
    void testGetDelegate_returnsNull_when_requestedStateIsPendingNotReady() {
        assertNull(factory.getDelegate(Enums.AgentMrdStateName.PENDING_NOT_READY));
    }

    @Test
    void testGetDelegate_returnsNull_when_requestedStateIsInterrupted() {
        assertNull(factory.getDelegate(Enums.AgentMrdStateName.INTERRUPTED));
    }

    @Test
    void testGetDelegate_returnsMrdStateNotReadyDelegate_when_requestedStateIsNotReady() {
        MrdStateDelegate delegate = factory.getDelegate(Enums.AgentMrdStateName.NOT_READY);
        assertEquals(MrdStateNotReady.class, delegate.getClass());
    }

    @Test
    void testGetDelegate_returnsMrdStateReadyDelegate_when_requestedStateIsReady() {
        MrdStateDelegate delegate = factory.getDelegate(Enums.AgentMrdStateName.READY);
        assertEquals(MrdStateReady.class, delegate.getClass());
    }

    @Test
    void testGetDelegate_returnsMrdStateNotActiveDelegate_when_requestedStateIsActive() {
        MrdStateDelegate delegate = factory.getDelegate(Enums.AgentMrdStateName.ACTIVE);
        assertEquals(MrdStateActive.class, delegate.getClass());
    }

    @Test
    void testGetDelegate_returnsMrdStateBusyDelegate_when_requestedStateIsBusy() {
        MrdStateDelegate delegate = factory.getDelegate(Enums.AgentMrdStateName.BUSY);
        assertEquals(MrdStateBusy.class, delegate.getClass());
    }
}