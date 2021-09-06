package com.ef.mediaroutingengine.services.utilities;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.python.util.PythonInterpreter;
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

    /**
     * Evaluate 2 boolean.
     *
     * @param criteria the criteria
     * @return the boolean
     */
    public static boolean evaluate2(String criteria) {
        try (PythonInterpreter pythonInterpreter = new PythonInterpreter()) {
            return pythonInterpreter.eval(criteria).asInt() != 0;
        } catch (Exception e) {
            LOGGER.error("Exception evaluating criteria", e);
        }
        return false;
    }

    private static ScriptEngine getScriptEngine() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager(null);
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        if (scriptEngine == null) {
            LOGGER.warn("Fail to initialize JavaScript script engine trying rhino now...");
            scriptEngine = scriptEngineManager.getEngineByName("rhino");
        }

        List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
        LOGGER.info("=============================");
        for (ScriptEngineFactory factory : factories) {
            LOGGER.info("{} {} {}", factory.getEngineName(), factory.getEngineVersion(), factory.getNames());
        }
        if (factories.isEmpty()) {
            LOGGER.info("No Script Engines found");
        }
        LOGGER.info("=============================");

        return scriptEngine;
    }
}
