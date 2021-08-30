package com.ef.mediaroutingengine.services.utilities;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.model.Expression;
import com.ef.mediaroutingengine.model.Step;
import com.ef.mediaroutingengine.model.Term;

/**
 * The type Step criteria builder.
 */
public class StepCriteriaBuilder {
    /**
     * Instantiates a new Step criteria builder.
     */
    private StepCriteriaBuilder() {

    }

    /**
     * Build step expression string.
     *
     * @param agent the agent
     * @param step  the step
     * @return the string
     */
    public static String buildFor(Agent agent, Step step) {
        StringBuilder result = new StringBuilder();
        for (Expression expression : step.getExpressions()) {
            if (expression.getPreExpressionCondition() != null) { // e.g. AND (exp1) -> operator before expression.
                result.append(convertLogicalOperator(expression.getPreExpressionCondition()));
            }
            result.append("(");
            result.append(buildFor(agent, expression));
            result.append(")");
        }

        return result.toString();
    }

    /**
     * Build expression expression string builder.
     *
     * @param agent      the agent
     * @param expression the expression
     * @return the string builder
     */
    private static StringBuilder buildFor(Agent agent, Expression expression) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Term term : expression.getTerms()) {
            if (term.getPreTermLogicalOperator() != null) { // e.g. AND term1 -> operator before term.
                stringBuilder.append(convertLogicalOperator(term.getPreTermLogicalOperator()));
            }
            stringBuilder.append(buildFor(agent, term));
        }
        return stringBuilder;
    }

    /**
     * Build term expression string.
     *
     * @param agent the agent
     * @param term  the term
     * @return the string
     */
    private static String buildFor(Agent agent, Term term) {
        AssociatedRoutingAttribute associatedRoutingAttribute = agent
                .findAssociatedRoutingAttributeById(term.getRoutingAttribute().getId());
        if (associatedRoutingAttribute == null) {
            return "false";
        }
        return associatedRoutingAttribute.getValue() + term.getRelationalOperator() + term.getValue();
    }

    /**
     * Convert logical operator string.
     *
     * @param s the s
     * @return the string
     */
    private static String convertLogicalOperator(String s) {
        switch (s) {
            case "AND":
            case "and":
                return "&&";
            case "OR":
            case "or":
                return "||";
            default:
                return null;
        }
    }
}
