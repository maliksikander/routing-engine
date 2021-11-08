package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.Min;

/**
 * The type Step entity.
 */
public class StepEntity {
    /**
     * The ID.
     */
    private UUID id;
    /**
     * The Expressions.
     */
    private List<ExpressionEntity> expressions;
    /**
     * The Timeout.
     */
    @Min(value = 0, message = "Negative integers not allowed for timeout value")
    private int timeout;

    /**
     * Instantiates a new Step entity.
     */
    public StepEntity() {
        this.id = UUID.randomUUID();
        this.expressions = new ArrayList<>();
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
     * Sets id.
     *
     * @param id the id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets expressions.
     *
     * @return the expressions
     */
    public List<ExpressionEntity> getExpressions() {
        return expressions;
    }

    /**
     * Sets expressions.
     *
     * @param expressionEntities the expression entities
     */
    public void setExpressions(List<ExpressionEntity> expressionEntities) {
        this.expressions = expressionEntities;
    }

    /**
     * Contains expression boolean.
     *
     * @param expressionEntity the expression entity
     * @return the boolean
     */
    public boolean containsExpression(ExpressionEntity expressionEntity) {
        return this.expressions.contains(expressionEntity);
    }

    /**
     * Add expression boolean.
     *
     * @param expressionEntity the expression entity
     * @return the boolean
     */
    public boolean addExpression(ExpressionEntity expressionEntity) {
        return this.expressions.add(expressionEntity);
    }

    /**
     * Remove expression boolean.
     *
     * @param expressionEntity the expression entity
     * @return the boolean
     */
    public boolean removeExpression(ExpressionEntity expressionEntity) {
        return this.expressions.remove(expressionEntity);
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

    @Override
    public String toString() {
        return "Step{"
                + "expressions=" + expressions
                + ", timeout=" + timeout
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StepEntity that = (StepEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
