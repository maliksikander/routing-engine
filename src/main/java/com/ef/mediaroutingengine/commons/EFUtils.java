package com.ef.mediaroutingengine.commons;

public class EFUtils {
    private EFUtils(){

    }

    /**
     * Method to check if String is null or empty.
     *
     * @param str String object
     * @return boolean
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
