package com.ef.mediaroutingengine.model;

import com.ef.mediaroutingengine.services.utilities.StepCriteriaBuilder;
import com.ef.mediaroutingengine.services.utilities.StepCriteriaEvaluator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Step.
 */
public class Step {
    /**
     * The Id.
     */
    private final UUID id;
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
        this.id = UUID.randomUUID();
        this.expressions = new ArrayList<>();
        this.associatedAgents = new ArrayList<>();
    }

    /**
     * Parameterized Constructor. Constructs object from StepEntity object.
     *
     * @param stepEntity Step entity object in configuration DB
     */
    public Step(StepEntity stepEntity) {
        this.id = stepEntity.getId();
        this.expressions = toExpressions(stepEntity.getExpressions());
        this.timeout = stepEntity.getTimeout();
        this.associatedAgents = new ArrayList<>();
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public UUID getId() {
        return id;
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
        synchronized (this.associatedAgents) {
            this.associatedAgents.clear();
            for (Agent agent : allAgents) {
                evaluateAssociatedAgentOnInsert(agent);
            }
        }
    }

    /**
     * Evaluate associated agent.
     *
     * @param agent the agent
     */
    public void evaluateAssociatedAgentOnInsert(Agent agent) {
        if (isAssociated(agent)) {
            synchronized (this.associatedAgents) {
                this.associatedAgents.add(agent);
            }
        }
    }

    /**
     * Evaluate associated agent on update.
     *
     * @param agent the agent
     */
    public void evaluateAssociatedAgentOnUpdate(Agent agent) {
        boolean isAssociated = isAssociated(agent);
        synchronized (this.associatedAgents) {
            int index = getIndexOf(agent);
            if (isAssociated && index == -1) {
                this.associatedAgents.add(agent);
            } else if (!isAssociated && index > -1) {
                this.associatedAgents.remove(index);
            }
        }
    }

    /**
     * Is associated boolean.
     *
     * @param agent the agent
     * @return the boolean
     */
    private boolean isAssociated(Agent agent) {
        String criteria = StepCriteriaBuilder.buildFor(agent, this);
        return StepCriteriaEvaluator.evaluate(criteria);
    }

    /**
     * Gets index of.
     *
     * @param agent the agent
     * @return the index of
     */
    private int getIndexOf(Agent agent) {
        return this.getIndexOf(agent.getId());
    }

    /**
     * Gets index of.
     *
     * @param id the id
     * @return the index of
     */
    private int getIndexOf(UUID id) {
        for (int i = 0; i < associatedAgents.size(); i++) {
            if (associatedAgents.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
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
        synchronized (this.associatedAgents) {
            int index = this.getIndexOf(id);
            if (index > -1) {
                associatedAgents.remove(index);
                return true;
            }
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
        synchronized (this.associatedAgents) {
            List<Agent> sorted = new ArrayList<>(this.associatedAgents);
            sorted.sort(Comparator.comparing((Agent o) -> o.getLastReadyStateChangeTimeFor(mrdId)));
            return sorted;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Step step = (Step) o;
        return Objects.equals(id, step.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
