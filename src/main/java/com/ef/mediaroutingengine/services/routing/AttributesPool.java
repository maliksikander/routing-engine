package com.ef.mediaroutingengine.services.routing;
//
//import com.ef.apps.entities.handlers.eventHandlers.AllAttributesEvent;
//import com.ef.apps.entities.handlers.eventHandlers.DeleteAttributeEvent;
//import com.ef.apps.entities.handlers.eventHandlers.NewAttributeEvent;
//import com.ef.apps.entities.handlers.eventHandlers.UpdateAttributeEvent;
//import com.fasterxml.jackson.databind.JsonNode;
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeSupport;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// * Created by Awais on 07-Aug-17.
// */
//public class AttributesPool {
//    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
//    private static AttributesPool pool;
//    private static List<Attribute> attributes;
//    private static Logger log = LogManager.getLogger(AttributesPool.class.getName());
//
//    private List<PropertyChangeListener> listeners = new LinkedList<>();
//
//    private AttributesPool() {
//        attributes = new LinkedList<>();
//        initialize();
//    }
//
//    public static AttributesPool getInstance() {
//        if (pool == null) {
//            pool = new AttributesPool();
//        }
//        return pool;
//    }
//
//    private void initialize() {
//        listeners.add(new NewAttributeEvent());
//        listeners.add(new UpdateAttributeEvent());
//        listeners.add(new DeleteAttributeEvent());
//        listeners.add(new AllAttributesEvent());
//
//        for (PropertyChangeListener listener : this.listeners) {
//            this.changeSupport.addPropertyChangeListener(listener);
//        }
//    }
//
//    public boolean addAttribute(Attribute attribute) {
//        boolean result = false;
//        String name = attribute.getName();
//        if (attribute != null && !attributeExists(name)) {
//            synchronized (attributes) {
//                result = attributes.add(attribute);
//            }
//        }
//        return result;
//    }
//
//    public boolean updateAttribute(Attribute attribute) {
//        boolean result = false;
//        String name = attribute.getName();
//        if (name != null && attributeExists(name)) {
//            for (Iterator<? extends Attribute> it = attributes.iterator(); it.hasNext(); ) {
//                Attribute currentAttribute = it.next();
//                if (name.equals(currentAttribute.getName())) {
//                    currentAttribute.setAttributeType(attribute.getAttributeType());
//                    currentAttribute.setAttributeValue(attribute.getAttributeValue());
//                    result = true;
//                    break;
//                }
//            }
//        }
//        return result;
//    }
//
//
//    public boolean attributeExists(String name) {
//        return attributes.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));
//    }
//
//    public boolean removeAttribute(String name) {
//        boolean result = false;
//        for (Iterator<? extends Attribute> it = attributes.iterator(); it.hasNext(); ) {
//            Attribute it_attr = it.next();
//            if (name != null && name.equals(it_attr.getName()) /*&& it_attr.isRemoveable()*/) {
//                synchronized (attributes) {
//                    it.remove();
//                }
//                log.info("Attribute {} deleted from Attributes Pool", it_attr.getName());
//                result = true;
//                break;
//            }
//        }
//        return result;
//    }
//
//    public boolean removeAttribute(Attribute attribute) {
//        String name = attribute.getName();
//        return removeAttribute(name);
//    }
//
//    // Returns copy of object instead of reference
//    public Attribute getAttribute(String name) {
//        Attribute attribute = null;
//        if (name != null && attributeExists(name)) {
//            for (Iterator<? extends Attribute> it = attributes.iterator(); it.hasNext(); ) {
//                Attribute currentAttribute = it.next();
//                if (name.equalsIgnoreCase(currentAttribute.getName())) {
//                    attribute = copyAttribute(currentAttribute);
//                    break;
//                }
//            }
//        }
//        return attribute;
//    }
//
//    // Returns copy of object instead of reference
//    public Attribute getAttribute(Attribute attribute) {
//        Attribute result = null;
//        String name = attribute.getName();
//        result = getAttribute(name);
//        return result;
//    }
//
//    public Attribute copyAttribute(Attribute right) {
//        Attribute attribute = new Attribute(right);
//        log.debug("returning cloned Attribute");
//        return attribute;
//    }
//
//    public boolean beginUsingAttribute(String attributeName) {
//        boolean result = false;
//        for (Iterator<? extends Attribute> it = attributes.iterator(); it.hasNext(); ) {
//            Attribute currentAttr = it.next();
//            if (attributeName.equalsIgnoreCase(currentAttr.getName())) {
//                currentAttr.beginUsing();
//                result = true;
//            }
//        }
//        return result;
//    }
//
//    public boolean endUsingAttribute(String attributeName) {
//        boolean result = false;
//        for (Iterator<? extends Attribute> it = attributes.iterator(); it.hasNext(); ) {
//            Attribute currentAttr = it.next();
//            if (attributeName.equalsIgnoreCase(currentAttr.getName())) {
//                currentAttr.endUsing();
//                result = true;
//            }
//        }
//        return result;
//    }
//
//    public boolean isRemoveable(String name) {
//        boolean removeable = false;
//        for (Attribute currentAttr : attributes) {
//            if (name.equalsIgnoreCase(currentAttr.getName())) {
//                removeable = (currentAttr.getUsageCount() <= 0);
//                break;
//            }
//        }
//        return removeable;
//    }
//
//    public String toString() {
//        StringBuilder result = new StringBuilder("Attributes Pool:\n");
//        for (Attribute attribute : attributes) {
//            result.append(attribute.toString()).append("\n");
//        }
//        result = new StringBuilder(result.substring(0, result.length() - 1));
//        return result.toString();
//    }
//
//    public List<Attribute> getAllAttributes() {
//        return attributes;
//    }
//
//    public void handleNewAttributeEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//
//    public void handleUpdateAttributeEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//
//    public void handleDeleteAttributeEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//
//    public void handleAllAttributesEvent(CommonDefs.EVENT_PROPERTIES property, JsonNode node) {
//        this.changeSupport.firePropertyChange(property.name(), null, node);
//    }
//}
