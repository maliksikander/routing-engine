package com.ef.mediaroutingengine.services;

import com.ef.cim.objectmodel.CCUser;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

public class ExpressionUtility {

    private ExpressionUtility() {

    }

    /**
     * Evaluates a postfix expression to evaluate agents associated with a Precision-Queue's Step.
     * It converts the infix expression to a postfix expression and evaluates the postfix expression.
     *
     * @param infixExpression list of infix expression's operands (list of CCUsers), operators (AND, OR)
     *                        and brackets
     * @return list of associated-agents.
     */
    public static List<CCUser> evaluateInfix(@NotNull List<Object> infixExpression) {
        List<Object> postFixList = ExpressionUtility.convertInfixToPostfix(infixExpression);
        return ExpressionUtility.evaluatePostfix(postFixList);
    }

    /**
     * Evaluates a postfix expression to evaluate agents associated with a Precision-Queue's Step.
     *
     * @param postfixExpression list of postfix expression's operands (list of CCUsers) and operators (AND, OR)
     * @return list of associated-agents.
     */
    @SuppressWarnings("unchecked")
    public static List<CCUser> evaluatePostfix(@NotNull List<Object> postfixExpression) {
        if (postfixExpression.isEmpty()) {
            return new ArrayList<>();
        }

        Deque<List<CCUser>> stack = new ArrayDeque<>();

        for (Object postfixEntity : postfixExpression) {
            if (isOperand(postfixEntity)) {
                List<CCUser> operand = (List<CCUser>) postfixEntity;
                stack.push(operand);
            } else { // If operator ("AND", "OR")
                List<CCUser> val1 = stack.pop();
                List<CCUser> val2 = stack.pop();
                String operator = (String) postfixEntity;

                switch (operator.toUpperCase()) {
                    case "AND":
                        stack.push(intersection(val2, val1));
                        break;
                    case "OR":
                        stack.push(union(val2, val1));
                        break;
                    default:
                        break;
                }
            }
        }
        return stack.pop(); // The last item on stack is the result of evaluation
    }

    /**
     * Convert the infix expression to the postfix expression.
     *
     * @param infixExpression list of operands (List of CCUser), Operators (AND, OR) and brackets.
     * @return postfix expression.
     */
    public static List<Object> convertInfixToPostfix(@NotNull List<Object> infixExpression) {
        List<Object> postFixList = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();

        for (Object infixEntity: infixExpression) {
            if (isOperand(infixEntity)) {
                postFixList.add(infixEntity);
            } else { // An operator or bracket
                String operatorOrBracket = (String) infixEntity;
                if (operatorOrBracket.equals("(")) {
                    stack.push(operatorOrBracket);
                } else if (operatorOrBracket.equals(")")) {
                    postFixList.addAll(popUntilStackElementIsOpeningBracket(stack));
                    stack.pop(); // Pop "(" after popping the operators
                } else { // We have an operator ("AND", "OR")
                    postFixList.addAll(popUntilPrecedenceGreaterThanStackElement(operatorOrBracket, stack));
                    stack.push(operatorOrBracket);
                }
            }
        }

        postFixList.addAll(popAll(stack));
        return postFixList;
    }

    private static boolean isOperand(Object infixEntity) {
        return infixEntity.getClass().equals(ArrayList.class);
    }

    private static List<Object> popUntilStackElementIsOpeningBracket(Deque<String> stack) {
        List<Object> elements = new ArrayList<>();
        while (!stack.isEmpty() && !stack.peek().equals("(")) {
            elements.add(stack.pop());
        }
        return elements;
    }

    private static int precedence(String operator) {
        switch (operator.toUpperCase()) {
            case "AND":
                return 2;
            case "OR":
                return 1;
            default:
                return 0;
        }
    }

    private static List<Object> popUntilPrecedenceGreaterThanStackElement(String operator, Deque<String> stack) {
        List<Object> elements = new ArrayList<>();
        while (!stack.isEmpty() && precedence(operator) <= precedence(stack.peek())) {
            elements.add(stack.pop());
        }
        return elements;
    }

    private static List<Object> popAll(Deque<String> stack) {
        List<Object> elements = new ArrayList<>();
        while (!stack.isEmpty()) {
            if (stack.peek().equals("(")) {
                throw new IllegalArgumentException("The infix expression is incorrect");
            }
            elements.add(stack.pop());
        }
        return elements;
    }

    private static List<CCUser> intersection(List<CCUser> a, List<CCUser> b) {
        return a.stream().distinct().filter(b::contains).collect(Collectors.toList());
    }

    private static List<CCUser> union(List<CCUser> a, List<CCUser> b) {
        List<CCUser> unionList = new ArrayList<>(a);
        unionList.addAll(b);
        return unionList;
    }
}