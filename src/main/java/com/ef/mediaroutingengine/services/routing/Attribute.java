//package com.ef.mediaroutingengine.services.routing;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
///**
// * Created by Awais on 07-Aug-17.
// */
//public class Attribute {
//
//    private String name;
//    private String description;
//    private CommonDefs.ATTRIBUTE_TYPE attributeType;
//    private Object attributeValue;
//    private int usageCount;
//
//    private static Logger log = LogManager.getLogger(Attribute.class.getName());
//
//
//    public Attribute(String name, CommonDefs.ATTRIBUTE_TYPE type, Object value) {
//        this.name = name;
//        this.attributeType = type;
//        this.attributeValue = value;
//        this.usageCount = 0;
//        log.info("New attribute created, name: {}, type: {}, value: {}", this.name, this.attributeType,
//                this.attributeType == CommonDefs.ATTRIBUTE_TYPE.BOOLEAN ? (boolean) attributeValue :
//                        (int) attributeValue);
//    }
//
//    public Attribute(Attribute attribute) {
//        this.name = attribute.getName();
//        this.description = attribute.description;
//        this.attributeType = attribute.getAttributeType();
//        this.attributeValue = attribute.getAttributeValue();
//        this.usageCount = 0;
//        log.info("Clone attribute created, name: {}, type: {}, value: {}", this.name, this.attributeType,
//                this.attributeType == CommonDefs.ATTRIBUTE_TYPE.BOOLEAN ? (boolean) attributeValue :
//                        (int) attributeValue);
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getName() {
//        return this.name;
//    }
//
//    public void setAttributeValue(Object value) {
//        this.attributeValue = value;
//    }
//
//    /*Unbox before using Object*/
//    public Object getAttributeValue() {
//        return this.attributeValue;
//    }
//
//    public void setAttributeType(CommonDefs.ATTRIBUTE_TYPE type) {
//        this.attributeType = type;
//    }
//
//    public CommonDefs.ATTRIBUTE_TYPE getAttributeType() {
//        return this.attributeType;
//    }
//
//    public void beginUsing() {
//        this.usageCount += 1;
//    }
//
//    public void endUsing() {
//        this.usageCount -= 1;
//    }
//
//    public int getUsageCount() {
//        return this.usageCount;
//    }
//
//    public boolean isRemoveable() {
//        return this.usageCount <= 0;
//    }
//
//    public String toString() {
//        String result = "";
//        result = result + "[Attribute Name: " + this.getName() + ", Type: "
//                + this.getAttributeType() + ", Value: "
//                + (this.getAttributeType() == CommonDefs.ATTRIBUTE_TYPE.BOOLEAN ? (boolean) this.getAttributeValue() :
//                        (int) this.getAttributeValue())
//                + ", Usage Count: " + this.getUsageCount() + "]";
//        return result;
//    }
//}
