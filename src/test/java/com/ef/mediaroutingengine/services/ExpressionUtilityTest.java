package com.ef.mediaroutingengine.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.KeycloakUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpressionUtilityTest {
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
        List<CCUser> operand1 = getListOfAgents(3);
        List<CCUser> operand2 = getListOfAgents(2);
        List<CCUser> operand3 = getListOfAgents(4);

        List<Object> infixExpression = Arrays.asList("(", operand1, "OR", operand2, "AND", operand3, ")");

        List<Object> evaluated = ExpressionUtility.convertInfixToPostfix(infixExpression);
        List<Object> expected = Arrays.asList(operand1, operand2, operand3, "AND", "OR");

        assertEquals(expected, evaluated);
    }

    @Test
    void testConvertInfixToPostfix_multipleExpressionsInStep() {
        List<CCUser> operand1 = getListOfAgents(3);
        List<CCUser> operand2 = getListOfAgents(2);
        List<CCUser> operand3 = getListOfAgents(4);
        List<CCUser> operand4 = getListOfAgents(2);
        List<CCUser> operand5 = getListOfAgents(3);

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

        List<CCUser> evaluated = ExpressionUtility.evaluatePostfix(postfixExpression);
        List<CCUser> expected = new ArrayList<>();

        assertEquals(expected, evaluated);
    }

    @Test
    void testEvaluatePostfix_singleExpressionInStep() {
        List<CCUser> operand1 = getListOfAgents(3);
        List<CCUser> operand2 = getListOfAgents(2);
        List<CCUser> operand3 = new ArrayList<>();
        operand3.add(operand2.get(0));

        List<Object> postfixExpression = Arrays.asList(operand1, operand2, operand3, "AND", "OR");

        List<CCUser> evaluated = ExpressionUtility.evaluatePostfix(postfixExpression);
        List<CCUser> expected = new ArrayList<>(operand1);
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

        List<CCUser> evaluated = ExpressionUtility.evaluateInfix(infixExpression);
        List<CCUser> expected = new ArrayList<>();

        assertEquals(expected, evaluated);
    }

    @Test
    void testEvaluateInfix_singleExpressionInStep() {
        List<CCUser> operand1 = getListOfAgents(3);
        List<CCUser> operand2 = getListOfAgents(2);
        List<CCUser> operand3 = new ArrayList<>();
        operand3.add(operand2.get(0));

        List<Object> infixExpression = Arrays.asList("(", operand1, "OR", operand2, "AND", operand3, ")");

        List<CCUser> evaluated = ExpressionUtility.evaluateInfix(infixExpression);
        List<CCUser> expected = new ArrayList<>(operand1);
        expected.add(operand2.get(0));

        assertEquals(expected, evaluated);
    }

    private List<CCUser> getListOfAgents(int noOfAgents) {
        List<CCUser> agents = new ArrayList<>();
        for (int i = 0; i < noOfAgents; i++) {
            KeycloakUser keycloakUser = new KeycloakUser();
            keycloakUser.setId(UUID.randomUUID());
            keycloakUser.setFirstName("agent" + i);
            keycloakUser.setUsername("agent" + i);
            keycloakUser.setRealm("realm1");

            CCUser agent = new CCUser();
            agent.setKeycloakUser(keycloakUser);
            agent.setId(keycloakUser.getId());

            agents.add(agent);
        }
        return agents;
    }
}
