package com.ef.mediaroutingengine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Expression entity.
 */
public class ExpressionEntity {

    /**
     * The Terms.
     */
    private List<TermEntity> terms;
    /**
     * The Pre expression condition.
     */
    private String preExpressionCondition;

    /**
     * Instantiates a new Expression entity.
     */
    public ExpressionEntity() {
        this.terms = new ArrayList<>();
    }

    /**
     * Gets terms.
     *
     * @return the terms
     */
    public List<TermEntity> getTerms() {
        return terms;
    }

    /**
     * Sets terms.
     *
     * @param termEntities the term entities
     */
    public void setTerms(List<TermEntity> termEntities) {
        this.terms = termEntities;
    }

    /**
     * Contains term boolean.
     *
     * @param termEntity the term entity
     * @return the boolean
     */
    public boolean containsTerm(TermEntity termEntity) {
        return this.terms.contains(termEntity);
    }

    /**
     * Add term boolean.
     *
     * @param termEntity the term entity
     * @return the boolean
     */
    public boolean addTerm(TermEntity termEntity) {
        return this.terms.add(termEntity);
    }

    /**
     * Remove term boolean.
     *
     * @param termEntity the term entity
     * @return the boolean
     */
    public boolean removeTerm(TermEntity termEntity) {
        return this.terms.remove(termEntity);
    }

    /**
     * Gets pre expression condition.
     *
     * @return the pre expression condition
     */
    public String getPreExpressionCondition() {
        return preExpressionCondition;
    }

    /**
     * Sets pre expression condition.
     *
     * @param preExpressionCondition the pre expression condition
     */
    public void setPreExpressionCondition(String preExpressionCondition) {
        this.preExpressionCondition = preExpressionCondition;
    }

    @Override
    public String toString() {
        return "Expression{"
                + "terms=" + terms
                + ", preExpressionCondition='" + preExpressionCondition + '\''
                + '}';
    }
}
