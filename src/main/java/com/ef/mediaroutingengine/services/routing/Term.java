package com.ef.mediaroutingengine.services.routing;
//
//public class Term {
//
//    private final CommonDefs.TERM_OPERATOR operator;
//    private final Attribute attribute;
//    private int proficiency;
//    private boolean booleanValue;
//
//    /**
//     * Constructor if value type boolean.
//     *
//     * @param attributeName name of attribute
//     * @param operator conditional operator
//     * @param booleanValue value of attribute
//     */
//    public Term(String attributeName, String operator, boolean booleanValue) {
//        // getAttributeByName() returns attribute copy, not reference
//        attribute = AttributesPool.getInstance().getAttribute(attributeName);
//        this.booleanValue = booleanValue;
//        this.operator = parseOperator(operator);
//    }
//
//    /**
//     * Constructor if attribute value is proficiency.
//     *
//     * @param attributeName name of attribute
//     * @param operator conditional operator
//     * @param proficiency value of proficiency
//     */
//    public Term(String attributeName, String operator, String proficiency) {
//        // getAttributeByName() returns attribute copy, not reference
//        attribute = AttributesPool.getInstance().getAttribute(attributeName);
//        this.proficiency = Integer.parseInt(proficiency);
//        this.operator = parseOperator(operator);
//    }
//
//    private CommonDefs.TERM_OPERATOR parseOperator(String operator) {
//        if (operator.equalsIgnoreCase("==")) {
//            return CommonDefs.TERM_OPERATOR.EQUAL;
//        } else if (operator.equalsIgnoreCase(">=") || operator.equalsIgnoreCase("=>")) {
//            return CommonDefs.TERM_OPERATOR.EQUAL_OR_GRATER;
//        } else if (operator.equalsIgnoreCase(">")) {
//            return CommonDefs.TERM_OPERATOR.GRATER;
//        } else if (operator.equalsIgnoreCase("<=")) {
//            return CommonDefs.TERM_OPERATOR.EQUAL_OR_LESS;
//        } else if (operator.equalsIgnoreCase("<")) {
//            return CommonDefs.TERM_OPERATOR.LESS;
//        } else if (operator.equalsIgnoreCase("!=") || operator.equalsIgnoreCase("<>")) {
//            return CommonDefs.TERM_OPERATOR.NOT_EQUAL;
//        }
//        return null;
//    }
//
//    private String getOperatorSymbol(CommonDefs.TERM_OPERATOR operator) {
//        String result = "";
//        switch (operator) {
//            case LESS:
//                result = "<";
//                break;
//            case EQUAL:
//                result = "==";
//                break;
//            case GRATER:
//                result = ">";
//                break;
//            case NOT_EQUAL:
//                result = "!=";
//                break;
//            case EQUAL_OR_LESS:
//                result = "<=";
//                break;
//            case EQUAL_OR_GRATER:
//                result = ">=";
//                break;
//            default:
//                break;
//        }
//        return result;
//    }
//
//    public String getClassName() {
//        return "This is term class";
//    }
//
//    public Attribute getAttribute() {
//        return this.attribute;
//    }
//
//    public CommonDefs.TERM_OPERATOR getOperator() {
//        return this.operator;
//    }
//
//    public Object getProficiency() {
//        return this.proficiency;
//    }
//
//    public boolean getBooleanValue() {
//        return this.booleanValue;
//    }
//
//    /**
//     * convert the object to a string.
//     *
//     * @return String
//     */
//    public String toString() {
//        String result = "(";
//        CommonDefs.ATTRIBUTE_TYPE type = this.attribute.getAttributeType();
//        switch (type) {
//            case PROFICIENCY:
//                result = result + this.attribute.getName() + " "
//                        + getOperatorSymbol(this.getOperator())
//                        + " " + this.proficiency;
//                break;
//            case BOOLEAN:
//                result = result + this.attribute.getName() + " "
//                        + getOperatorSymbol(this.getOperator())
//                        + " " + this.booleanValue;
//                break;
//            default:
//                break;
//        }
//        result = result + ")";
//        return result;
//    }
//}
