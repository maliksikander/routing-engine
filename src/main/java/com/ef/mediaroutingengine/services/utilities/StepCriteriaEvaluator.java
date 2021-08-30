package com.ef.mediaroutingengine.services.utilities;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Step criteria evaluator.
 */
public class StepCriteriaEvaluator {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StepCriteriaEvaluator.class);

    /**
     * Instantiates a new Step criteria evaluator.
     */
    private StepCriteriaEvaluator() {

    }

    /**
     * The constant SCRIPT_ENGINE.
     */
    private static final ScriptEngine SCRIPT_ENGINE = getScriptEngine();

    /**
     * Evaluate boolean.
     *
     * @param criteria the criteria
     * @return the boolean
     */
    public static boolean evaluate(String criteria) {
        try {
            return (boolean) SCRIPT_ENGINE.eval(criteria);
        } catch (Exception e) {
            LOGGER.error("Exception evaluating criteria", e);
            return false;
        }
    }

    /**
     * Gets script engine.
     *
     * @return the script engine
     */
    private static ScriptEngine getScriptEngine() {
        return new ScriptEngineManager().getEngineByName("JavaScript");
    }
}
