package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.services.utilities.StepCriteriaBuilder;
import com.ef.mediaroutingengine.services.utilities.StepCriteriaEvaluator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Step.
 */
public class Step {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Step.class);
    /**
     * The Expressions.
     */
    private List<Expression> expressions;
    /**
     * The Timeout.
     */
    private int timeout;
    /**
     * The Associated agents.
     */
    private final List<Agent> associatedAgents;

    /**
     * Instantiates a new Step.
     */
    public Step() {
        this.expressions = new ArrayList<>();
        this.associatedAgents = new ArrayList<>();
    }

    /**
     * Parameterized Constructor. Constructs object from StepEntity object.
     *
     * @param stepEntity Step entity object in configuration DB
     */
    public Step(StepEntity stepEntity) {
        this.expressions = toExpressions(stepEntity.getExpressions());
        this.timeout = stepEntity.getTimeout();
        this.associatedAgents = new ArrayList<>();
    }

    /**
     * To expressions list.
     *
     * @param expressionEntities the expression entities
     * @return the list
     */
    private List<Expression> toExpressions(List<ExpressionEntity> expressionEntities) {
        if (expressionEntities == null) {
            return new ArrayList<>();
        }
        List<Expression> elements = new ArrayList<>();
        for (ExpressionEntity entity : expressionEntities) {
            elements.add(new Expression(entity));
        }
        return elements;
    }

    /**
     * Gets expressions.
     *
     * @return the expressions
     */
    public List<Expression> getExpressions() {
        return expressions;
    }

    /**
     * Sets expressions.
     *
     * @param expressions the expressions
     */
    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Evaluate associated agents.
     *
     * @param allAgents the all agents
     */
    public void evaluateAssociatedAgents(List<Agent> allAgents) {
        this.associatedAgents.clear();
        for (Agent agent : allAgents) {
            String criteria = StepCriteriaBuilder.buildFor(agent, this);
            boolean result = StepCriteriaEvaluator.evaluate(criteria);
            if (result) {
                this.associatedAgents.add(agent);
            }
        }
    }

    /**
     * Gets associated agents.
     *
     * @return the associated agents
     */
    public List<Agent> getAssociatedAgents() {
        return associatedAgents;
    }

    /**
     * Removes associated agent by id.
     *
     * @param id id of the agent to be removed
     * @return true if found and removed, false otherwise
     */
    public boolean removeAssociatedAgent(UUID id) {
        int index = -1;
        for (int i = 0; i < associatedAgents.size(); i++) {
            if (associatedAgents.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            associatedAgents.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Sorts and returns the associated agent list by Agent-Selection-Criteria.
     *
     * @param agentSelectionCriteria The selection criteria on which to sort the agents
     * @param mrdId                  the mrd id
     * @return the sorted or ordered associated agents list.
     */
    public List<Agent> orderAgentsBy(AgentSelectionCriteria agentSelectionCriteria, UUID mrdId) {
        switch (agentSelectionCriteria) {
            case LONGEST_AVAILABLE:
                return sortAsLongestAvailable(mrdId);
            case MOST_SKILLED:
            case LEAST_SKILLED:
            case DEFAULT:
                return this.associatedAgents;
            default:
                LOGGER.error("Switch's default case, returning null");
                return new ArrayList<>();
        }
    }

    /**
     * Sort as longest available list.
     *
     * @param mrdId the mrd id
     * @return the list
     */
    private List<Agent> sortAsLongestAvailable(UUID mrdId) {
        List<Agent> sorted = new ArrayList<>(this.associatedAgents);
        sorted.sort(Comparator.comparing((Agent o) -> o.getLastReadyStateChangeTimeFor(mrdId)));
        return sorted;
    }
}
