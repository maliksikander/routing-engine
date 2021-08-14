package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.RoutingAttribute;

/**
 * The type Term.
 */
public class Term {
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
     * The Pre term logical operator.
     */
    private String preTermLogicalOperator;

    /**
     * Instantiates a new Term.
     */
    public Term() {

    }

    /**
     * Parametrized constructor. Constructs object from TermEntity object.
     *
     * @param termEntity the Term entity object stored in configurations DB
     */
    public Term(TermEntity termEntity) {
        this.routingAttribute = termEntity.getRoutingAttribute();
        this.relationalOperator = termEntity.getRelationalOperator();
        this.value = termEntity.getValue();
        this.preTermLogicalOperator = termEntity.getPreTermCondition();
    }

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
     * Gets pre term logical operator.
     *
     * @return the pre term logical operator
     */
    public String getPreTermLogicalOperator() {
        return preTermLogicalOperator;
    }

    /**
     * Sets pre term logical operator.
     *
     * @param preTermLogicalOperator the pre term logical operator
     */
    public void setPreTermLogicalOperator(String preTermLogicalOperator) {
        this.preTermLogicalOperator = preTermLogicalOperator;
    }
}
