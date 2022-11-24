package br.com.gredom.webscraping.util;

public class Strings {

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean nonEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return isEmpty(str) || str.trim().isEmpty();
    }

    public static boolean nonBlank(String str) {
        return !isBlank(str);
    }
}