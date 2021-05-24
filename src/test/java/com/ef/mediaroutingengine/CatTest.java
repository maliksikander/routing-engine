package com.ef.mediaroutingengine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatTest {
    private PropertyChangeSupport changeSupport;
    private CatListener catListener;

    @BeforeEach
    void setUp() {
        changeSupport = new PropertyChangeSupport(this);
        catListener = new CatListener();
    }

    @Test
    void testListener1() {
        changeSupport.addPropertyChangeListener(catListener);
        changeSupport.addPropertyChangeListener(catListener);
        changeSupport.firePropertyChange("property1", null, "Ahmad");
        System.out.println(Arrays.toString(changeSupport.getPropertyChangeListeners()));
    }

    @Test
    void testListener2() {
        String p1 = "property1";
        String p2 = "property2";

        changeSupport.addPropertyChangeListener(p1, catListener);
        if (!listenerExists(p1, catListener)) {
            changeSupport.addPropertyChangeListener(p1, catListener);
        }

        if (!listenerExists(catListener)) {
            changeSupport.addPropertyChangeListener(p2, catListener);
        }

        changeSupport.firePropertyChange("property1", null, "Ahmad");
        changeSupport.firePropertyChange("property2", null, "Bappi");
        System.out.println(Arrays.toString(changeSupport.getPropertyChangeListeners()));
    }

    private boolean listenerExists(String property, PropertyChangeListener listener) {
        PropertyChangeListener [] listeners = changeSupport.getPropertyChangeListeners(property);
        return listenerExistsIn(listeners, listener);
    }

    private boolean listenerExists(PropertyChangeListener listener) {
        PropertyChangeListener [] listeners = changeSupport.getPropertyChangeListeners();
        return listenerExistsIn(listeners, listener);
    }

    private boolean listenerExistsIn(PropertyChangeListener [] listeners, PropertyChangeListener listener) {
        for (PropertyChangeListener element: listeners) {
            if (element.equals(listener)) {
                return true;
            }
        }
        return false;
    }
}
