package com.ef.mediaroutingengine.eventlisteners;

import com.ef.mediaroutingengine.model.Enums;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DispatchEWT implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equalsIgnoreCase(Enums.CommandProperties.DISPATCH_EWT.toString())) {
            System.out.println("EventHandler for DispatchEWT");
        }
    }

}
