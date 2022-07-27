package com.ef.mediaroutingengine.routing.dto;

import com.ef.cim.objectmodel.CCUser;

/**
 * The type Change state request.
 */
public class ChangeStateRequest {

    /**
     * The Cc user.
     */
    CCUser ccUser;
    /**
     * The State.
     */
    String state;

    /**
     * Gets cc user.
     *
     * @return the cc user
     */
    public CCUser getCcUser() {
        return ccUser;
    }

    /**
     * Sets cc user.
     *
     * @param ccUser the cc user
     */
    public void setCcUser(CCUser ccUser) {
        this.ccUser = ccUser;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the state
     */
    public void setState(String state) {
        this.state = state;
    }
}
