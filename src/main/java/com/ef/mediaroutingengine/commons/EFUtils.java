package com.ef.mediaroutingengine.commons;

public class EFUtils {


    /**
     * Method to check if String is null or empty
     *
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
