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
     * The constant SCRIPT_ENGINE.
     */
    private static final ScriptEngine SCRIPT_ENGINE = getScriptEngine();

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
            LOGGER.info("INSIDE EVALUATOR | CRITERIA: {}", criteria);
            if (SCRIPT_ENGINE != null) {
                return (boolean) SCRIPT_ENGINE.eval(criteria);
            } else {
                LOGGER.error("SCRIPT ENGINE OBJECT IS NULL");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Exception evaluating criteria", e);
            return false;
        }
    }

    private static ScriptEngine getScriptEngine() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        if (scriptEngine == null) {
            LOGGER.warn("Fail to initialize JavaScript script engine trying rhino now...");
            scriptEngine = scriptEngineManager.getEngineByName("rhino");
        }
        return scriptEngine;
    }
}
