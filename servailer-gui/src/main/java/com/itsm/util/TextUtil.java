package com.itsm.util;

/**
 * Created by anpiakhota on 30.12.16.
 */
public class TextUtil {

    /**
     * http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return str.matches("^(?:(?:\\-{1})?\\d+(?:\\.{1}\\d+)?)$");
    }

}
