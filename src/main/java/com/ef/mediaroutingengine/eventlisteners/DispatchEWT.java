package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.CommonEnums;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DispatchEWT implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(CommonEnums.CommandProperties.DISPATCH_EWT.toString())) {
            System.out.println("EventHandler for DispatchEWT");
        }
    }

}
