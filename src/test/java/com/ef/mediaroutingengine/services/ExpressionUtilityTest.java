package com.ef.mediaroutingengine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import com.ef.mediaroutingengine.model.Agent;
import com.ef.mediaroutingengine.services.utilities.ExpressionUtility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpressionUtilityTest {
    private int agentNumber = 1;

    @Test
    void testConvertInfixToPostfix_throwsNullPointerException_when_nullArgument() {
        assertThrows(NullPointerException.class,
                () -> ExpressionUtility.convertInfixToPostfix(null));
    }

    @Test
    void testConvertInfixToPostfix_returnsEmptyExpression_when_emptyInfixExpression() {
        List<Object> infixExpression = new ArrayList<>();

        List<Object> evaluated = ExpressionUtility.convertInfixToPostfix(infixExpression);
        List<Object> expected = new ArrayList<>();

        assertEquals(expected, evaluated);
    }

    @Test
    void testConvertInfixToPostfix_singleExpressionInStep() {
        List<Agent> operand1 = getListOfAgents(3);
        List<Agent> operand2 = getListOfAgents(2);
        List<Agent> operand3 = getListOfAgents(4);

        List<Object> infixExpression = Arrays.asList("(", operand1, "OR", operand2, "AND", operand3, ")");

        List<Object> evaluated = ExpressionUtility.convertInfixToPostfix(infixExpression);
        List<Object> expected = Arrays.asList(operand1, operand2, operand3, "AND", "OR");

        assertEquals(expected, evaluated);
    }

    @Test
    void testConvertInfixToPostfix_multipleExpressionsInStep() {
        List<Agent> operand1 = getListOfAgents(3);
        List<Agent> operand2 = getListOfAgents(2);
        List<Agent> operand3 = getListOfAgents(4);
        List<Agent> operand4 = getListOfAgents(2);
        List<Agent> operand5 = getListOfAgents(3);

        List<Object> infixExpression = Arrays.asList("(", operand1, "OR", operand2, "AND", operand3, ")",
                "OR", "(", operand4, "AND", operand5, ")");

        List<Object> evaluated = ExpressionUtility.convertInfixToPostfix(infixExpression);
        List<Object> expected = Arrays.asList(operand1, operand2, operand3, "AND", "OR", operand4, operand5,
                "AND", "OR");

        assertEquals(expected, evaluated);
    }

    @Test
    void testEvaluatePostfix_throwsNullPointerException_when_nullArgument() {
        assertThrows(NullPointerException.class,
                () -> ExpressionUtility.evaluatePostfix(null));
    }

    @Test
    void testEvaluatePostfix_returnEmptyList_when_emptyExpression() {
        List<Object> postfixExpression = new ArrayList<>();

        List<Agent> evaluated = ExpressionUtility.evaluatePostfix(postfixExpression);
        List<Agent> expected = new ArrayList<>();

        assertEquals(expected, evaluated);
    }

    @Test
    void testEvaluatePostfix_singleExpressionInStep() {
        List<Agent> operand1 = getListOfAgents(3);
        List<Agent> operand2 = getListOfAgents(2);
        List<Agent> operand3 = new ArrayList<>();
        operand3.add(operand2.get(0));

        List<Object> postfixExpression = Arrays.asList(operand1, operand2, operand3, "AND", "OR");

        List<Agent> evaluated = ExpressionUtility.evaluatePostfix(postfixExpression);
        List<Agent> expected = new ArrayList<>(operand1);
        expected.add(operand2.get(0));

        assertEquals(expected, evaluated);
    }

    @Test
    void testEvaluateInfix_throwsNullPointerException_when_nullArgument() {
        assertThrows(NullPointerException.class,
                () -> ExpressionUtility.evaluateInfix(null));
    }

    @Test
    void testEvaluateInfix_returnEmptyList_when_emptyExpression() {
        List<Object> infixExpression = new ArrayList<>();

        List<Agent> evaluated = ExpressionUtility.evaluateInfix(infixExpression);
        List<Agent> expected = new ArrayList<>();

        assertEquals(expected, evaluated);
    }

    @Test
    void testEvaluateInfix_singleExpressionInStep() {
        List<Agent> operand1 = getListOfAgents(3);
        List<Agent> operand2 = getListOfAgents(2);
        List<Agent> operand3 = new ArrayList<>();
        operand3.add(operand2.get(0));

        List<Object> infixExpression = Arrays.asList("(", operand1, "OR", operand2, "AND", operand3, ")");

        List<Agent> evaluated = ExpressionUtility.evaluateInfix(infixExpression);
        List<Agent> expected = new ArrayList<>(operand1);
        expected.add(operand2.get(0));

        assertEquals(expected, evaluated);
    }

    private List<Agent> getListOfAgents(int noOfAgents) {
        List<Agent> agents = new ArrayList<>();
        for (int i = 0; i < noOfAgents; i++) {
            agents.add(new Agent(createNewCcUserInstance()));
        }
        return agents;
    }

    private CCUser createNewCcUserInstance() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(UUID.randomUUID());
        keycloakUser.setFirstName("Agent");
        keycloakUser.setLastName(String.valueOf(agentNumber));
        keycloakUser.setRealm("Realm1");
        keycloakUser.setUsername("Agent " + agentNumber);
        agentNumber++;

        CCUser ccUser = new CCUser();
        ccUser.setKeycloakUser(keycloakUser);
        ccUser.setId(keycloakUser.getId());
        ccUser.setAssociatedRoutingAttributes(new ArrayList<>());
        return ccUser;
    }
}
