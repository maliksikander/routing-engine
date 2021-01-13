package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;

public class FindAgentImpl implements FindAgent{
    public CCUser find(boolean flag){
        if(flag) {
            CCUser ccUser = new CCUser();
            ccUser.setFirstName("Ahmad");
            ccUser.setLastName("Bappi");
            return ccUser;
        }
        return null;
    }
}
