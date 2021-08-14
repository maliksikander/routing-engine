package com.ef.mediaroutingengine.services.utilities;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepCriteriaEvaluator {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StepCriteriaEvaluator.class);

    private StepCriteriaEvaluator() {

    }

    /**
     * The constant SCRIPT_ENGINE.
     */
    private static final ScriptEngine SCRIPT_ENGINE = getScriptEngine();

    /**
     * Evaluate boolean.
     *
     * @param expression the expression
     * @return the boolean
     */
    public static boolean evaluate(String expression) {
        try {
            return (boolean) SCRIPT_ENGINE.eval(expression);
        } catch (Exception e) {
            LOGGER.error("Exception evaluating expression", e);
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
