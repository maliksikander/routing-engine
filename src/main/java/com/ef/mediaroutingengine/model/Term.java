package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.RoutingAttribute;

public class Term {

    private RoutingAttribute routingAttribute;
    private String conditionOperator;
    private int value;
    private String preTermCondition;

    public RoutingAttribute getRoutingAttribute() {
        return routingAttribute;
    }

    public void setRoutingAttribute(RoutingAttribute routingAttribute) {
        this.routingAttribute = routingAttribute;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
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
                + ", conditionOperator='" + conditionOperator + '\''
                + ", value='" + value + '\''
                + ", preTermCondition='" + preTermCondition + '\''
                + '}';
    }
}
