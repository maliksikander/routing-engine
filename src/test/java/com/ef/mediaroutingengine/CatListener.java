package com.ef.mediaroutingengine;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CatListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = (String) evt.getNewValue();
        System.out.println("Listener: " + name);
    }
}
