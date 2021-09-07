package com.ef.mediaroutingengine.services.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * The type Step criteria evaluator.
 */
public class StepCriteriaEvaluator {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StepCriteriaEvaluator.class);
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * Instantiates a new Step criteria evaluator.
     */
    private StepCriteriaEvaluator() {

    }

    /**
     * Evaluate boolean.
     *
     * @param criteria the criteria
     * @return the boolean
     */
    public static boolean evaluate(String criteria) {
        try {
            Boolean result = (Boolean) PARSER.parseExpression(criteria).getValue();
            if (result != null) {
                return result;
            }
            return false;
        } catch (Exception ex) {
            LOGGER.error("Exception evaluating criteria", ex);
            return false;
        }
    }
}
