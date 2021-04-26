package com.ef.mediaroutingengine.model;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Term {
    private RoutingAttribute routingAttribute;
    private String relationalOperator;
    private int value;
    private String preTermLogicalOperator;

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

    public String getPreTermLogicalOperator() {
        return preTermLogicalOperator;
    }

    public void setPreTermLogicalOperator(String preTermLogicalOperator) {
        this.preTermLogicalOperator = preTermLogicalOperator;
    }

    /**
     * Evaluates the list of agents associated with this term.
     *
     * @param allAgents list of all agents in the configuration db.
     */
    public List<UUID> evaluateAssociatedAgents(List<Agent> allAgents) {
        List<UUID> associatedAgents = new ArrayList<>();

        for (Agent agent: allAgents) {
            List<AssociatedRoutingAttribute> associatedRoutingAttributes = agent.getAssociatedRoutingAttributes();
            if (associatedRoutingAttributes == null) {
                continue;
            }
            for (AssociatedRoutingAttribute associatedRoutingAttribute: associatedRoutingAttributes) {
                if (this.routingAttribute.equals(associatedRoutingAttribute.getRoutingAttribute())
                        // Example 3 (agent's value) > 4 (value in term)
                        && applyRelationalOperator(associatedRoutingAttribute.getValue(), this.value)) {
                    associatedAgents.add(agent.getId());
                    break;
                }
            }
        }

        return associatedAgents;
    }

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
