package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.RoutingAttribute;

/**
 * The type Term entity.
 */
public class TermEntity {

    /**
     * The Routing attribute.
     */
    private RoutingAttribute routingAttribute;
    /**
     * The Relational operator.
     */
    private String relationalOperator;
    /**
     * The Value.
     */
    private int value;
    /**
     * The Pre term condition.
     */
    private String preTermCondition;

    /**
     * Gets routing attribute.
     *
     * @return the routing attribute
     */
    public RoutingAttribute getRoutingAttribute() {
        return routingAttribute;
    }

    /**
     * Sets routing attribute.
     *
     * @param routingAttribute the routing attribute
     */
    public void setRoutingAttribute(RoutingAttribute routingAttribute) {
        this.routingAttribute = routingAttribute;
    }

    /**
     * Gets relational operator.
     *
     * @return the relational operator
     */
    public String getRelationalOperator() {
        return relationalOperator;
    }

    /**
     * Sets relational operator.
     *
     * @param relationalOperator the relational operator
     */
    public void setRelationalOperator(String relationalOperator) {
        this.relationalOperator = relationalOperator;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Gets pre term condition.
     *
     * @return the pre term condition
     */
    public String getPreTermCondition() {
        return preTermCondition;
    }

    /**
     * Sets pre term condition.
     *
     * @param preTermCondition the pre term condition
     */
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
