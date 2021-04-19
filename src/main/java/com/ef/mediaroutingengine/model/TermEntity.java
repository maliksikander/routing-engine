package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.RoutingAttribute;

public class TermEntity {

    private RoutingAttribute routingAttribute;
    private String relationalOperator;
    private int value;
    private String preTermCondition;

    public RoutingAttribute getRoutingAttribute() {
        return routingAttribute;
    }

    public void setRoutingAttribute(RoutingAttribute routingAttribute) {
        this.routingAttribute = routingAttribute;
    }

    public String getRelationalOperator() {
        return relationalOperator;
    }

    public void setRelationalOperator(String relationalOperator) {
        this.relationalOperator = relationalOperator;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getPreTermCondition() {
        return preTermCondition;
    }

    public void setPreTermCondition(String preTermCondition) {
        this.preTermCondition = preTermCondition;
    }

    @Override
    public String toString() {
        return "Term{"
                + "routingAttribute=" + routingAttribute
                + ", conditionOperator='" + relationalOperator + '\''
                + ", value='" + value + '\''
                + ", preTermCondition='" + preTermCondition + '\''
                + '}';
    }
}
