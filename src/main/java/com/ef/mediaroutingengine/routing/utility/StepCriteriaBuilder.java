package com.ef.mediaroutingengine.routing.utility;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.Expression;
import com.ef.mediaroutingengine.routing.model.Step;
import com.ef.mediaroutingengine.routing.model.Term;

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
     * Build step criteria string.
     *
     * @param agent the agent
     * @param step  the step
     * @return the string
     */
    public static String buildCriteria(Agent agent, Step step) {
        StringBuilder result = new StringBuilder();
        for (Expression expression : step.getExpressions()) {
            if (expression.getPreExpressionCondition() != null) { // e.g. AND (exp1) -> operator before expression.
                result.append(convertLogicalOperator(expression.getPreExpressionCondition()));
            }
            result.append("(");
            result.append(buildCriteria(agent, expression));
            result.append(")");
        }
        return result.toString();
    }

    /**
     * Build expression criteria string.
     *
     * @param agent      the agent
     * @param expression the expression
     * @return the string builder
     */
    private static StringBuilder buildCriteria(Agent agent, Expression expression) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Term term : expression.getTerms()) {
            if (term.getPreTermLogicalOperator() != null) { // e.g. AND term1 -> operator before term.
                stringBuilder.append(convertLogicalOperator(term.getPreTermLogicalOperator()));
            }
            stringBuilder.append(buildCriteria(agent, term));
        }
        return stringBuilder;
    }

    /**
     * Build term criteria string.
     *
     * @param agent the agent
     * @param term  the term
     * @return the string
     */
    private static String buildCriteria(Agent agent, Term term) {
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
        return switch (s) {
            case "AND", "and" -> "&&";
            case "OR", "or" -> "||";
            default -> null;
        };
    }
}
