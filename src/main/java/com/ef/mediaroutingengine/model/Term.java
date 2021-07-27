package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.RoutingAttribute;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Evaluates the list of agents associated with this term.
     *
     * @param allAgents list of all agents in the configuration db.
     * @return the list
     */
    public List<Agent> evaluateAssociatedAgents(List<Agent> allAgents) {
        List<Agent> associatedAgents = new ArrayList<>();

        for (Agent agent : allAgents) {
            List<AssociatedRoutingAttribute> associatedRoutingAttributes = agent.getAssociatedRoutingAttributes();
            if (associatedRoutingAttributes == null) {
                continue;
            }
            for (AssociatedRoutingAttribute associatedRoutingAttribute : associatedRoutingAttributes) {
                if (this.routingAttribute.equals(associatedRoutingAttribute.getRoutingAttribute())
                        // Example 3 (agent's value) > 4 (value in term)
                        && applyRelationalOperator(associatedRoutingAttribute.getValue(), this.value)) {
                    associatedAgents.add(agent);
                    break;
                }
            }
        }

        return associatedAgents;
    }

    /**
     * Apply relational operator boolean.
     *
     * @param a the a
     * @param b the b
     * @return the boolean
     */
    private boolean applyRelationalOperator(int a, int b) {
        switch (this.relationalOperator) {
            case "==":
                return a == b;
            case "!=":
                return a != b;
            case "<":
                return a < b;
            case ">":
                return a > b;
            case "<=":
            case "=<":
                return a <= b;
            case ">=":
            case "=>":
                return a >= b;
            default:
                return false;
        }
    }
}
