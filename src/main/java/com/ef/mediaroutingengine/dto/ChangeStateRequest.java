package com.ef.mediaroutingengine.dto;

import com.ef.cim.objectmodel.CCUser;

public class ChangeStateRequest {

    CCUser ccUser;
    String state;

    public CCUser getCcUser() {
        return ccUser;
    }

    public void setCcUser(CCUser ccUser) {
        this.ccUser = ccUser;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
