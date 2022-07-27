package com.ef.mediaroutingengine;

import com.ef.cim.objectmodel.AssociatedRoutingAttribute;
import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.RoutingAttributeType;
import com.ef.mediaroutingengine.routing.model.Agent;
import com.ef.mediaroutingengine.routing.model.Expression;
import com.ef.mediaroutingengine.routing.model.Step;
import com.ef.mediaroutingengine.routing.model.Term;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MyTesting {
    private final List<RoutingAttribute> routingAttributes = new ArrayList<>();
    private final List<Agent> agents = new ArrayList<>();

    @Test
    void testEvaluation() {
        loadRoutingAttributesList();
        loadAgents();

        List<Term> terms1 = new ArrayList<>();
        terms1.add(getTerm(null, "English", ">=", 5));
        terms1.add(getTerm("OR", "Marketing", "==", 1));

        List<Term> terms2 = new ArrayList<>();
        terms2.add(getTerm(null, "English", "<", 4));
        terms2.add(getTerm("AND", "Sales", "==", 1));

        List<Expression> expressions = new ArrayList<>();
        expressions.add(getExpression(null, terms1));
        expressions.add(getExpression("OR", terms2));


        Step step = getStep(expressions, 30);
//        List<Object> infix = step.getInfixExpression(this.agents.get(0));
//        System.out.println("Result: " + ExpressionEvaluator.evaluateInfix(infix));
        step.evaluateAssociatedAgents(this.agents);
        System.out.print("Agents: ");
        for (Agent agent : step.getAssociatedAgents()) {
            System.out.print(agent.getId() + " ");
        }
        System.out.println(".");
    }

    @Test
    void testDeletionInList() {
        List<String> letters = new ArrayList<>();
        letters.add("A");
        letters.add("B");
        letters.add("C");
        letters.add("D");
        letters.add("E");
        int index = -1;
        for (int i = 0; i < letters.size(); i++) {
            if (letters.get(i).equals("D")) {
                index = i;
                break;
            }
        }
        System.out.println(index);
        if (index > -1) {
            letters.remove(index);
        }
        System.out.println(letters);
    }

    private RoutingAttribute findByName(String name) {
        for (RoutingAttribute routingAttribute : this.routingAttributes) {
            if (routingAttribute.getName().equals(name)) {
                return routingAttribute;
            }
        }
        return null;
    }

    private Step getStep(List<Expression> expressions, int timeout) {
        Step step = new Step();
        step.setExpressions(expressions);
        step.setTimeout(timeout);
        return step;
    }

    private Expression getExpression(String preExpressionCondition, List<Term> terms) {
        Expression expression = new Expression();
        expression.setPreExpressionCondition(preExpressionCondition);
        expression.setTerms(terms);
        return expression;
    }

    private Term getTerm(String preTermCondition, String attribute, String operator, int value) {
        Term term = new Term();
        term.setPreTermLogicalOperator(preTermCondition);
        term.setRoutingAttribute(this.findByName(attribute));
        term.setRelationalOperator(operator);
        term.setValue(value);
        return term;
    }

    private void loadAgents() {
        List<AssociatedRoutingAttribute> associatedRoutingAttributes = new ArrayList<>();
        associatedRoutingAttributes.add(getAssociatedRoutingAttribute("English", 7));
        associatedRoutingAttributes.add(getAssociatedRoutingAttribute("Marketing", 1));
        this.agents.add(getAgent("Ahmad", "Bappi", associatedRoutingAttributes));
    }

    private void loadRoutingAttributesList() {
        routingAttributes.add(getRoutingAttribute("English", RoutingAttributeType.PROFICIENCY_LEVEL,
                5));
        routingAttributes.add(getRoutingAttribute("Marketing", RoutingAttributeType.BOOLEAN, 1));
        routingAttributes.add(getRoutingAttribute("Sales", RoutingAttributeType.BOOLEAN, 1));
        routingAttributes.add(getRoutingAttribute("Science", RoutingAttributeType.PROFICIENCY_LEVEL,
                4));
    }

    private Agent getAgent(String firstname, String lastname,
                           List<AssociatedRoutingAttribute> associatedRoutingAttributes) {
        KeycloakUser keycloakUser = getKeycloakUser(firstname, lastname);
        CCUser ccUser = new CCUser();
        ccUser.setId(keycloakUser.getId());
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.setAssociatedRoutingAttributes(associatedRoutingAttributes);
        return new Agent(ccUser);
    }

    private AssociatedRoutingAttribute getAssociatedRoutingAttribute(String attribute, int value) {
        AssociatedRoutingAttribute associatedRoutingAttribute = new AssociatedRoutingAttribute();
        associatedRoutingAttribute.setRoutingAttribute(findByName(attribute));
        associatedRoutingAttribute.setValue(value);
        return associatedRoutingAttribute;
    }

    private KeycloakUser getKeycloakUser(String firstname, String lastname) {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        keycloakUser.setRoles(new ArrayList<>());
        keycloakUser.setPermittedResources(null);
        keycloakUser.setRealm("realm1");
        keycloakUser.setFirstName(firstname);
        keycloakUser.setLastName(lastname);
        keycloakUser.setUsername(firstname + "-" + lastname);
        return keycloakUser;
    }

    private RoutingAttribute getRoutingAttribute(String name, RoutingAttributeType type, int defaultValue) {
        RoutingAttribute routingAttribute = new RoutingAttribute();
        routingAttribute.setId(UUID.randomUUID().toString());
        routingAttribute.setName(name);
        routingAttribute.setDescription("Description-" + name);
        routingAttribute.setType(type);
        routingAttribute.setDefaultValue(defaultValue);
        return routingAttribute;
    }
}
