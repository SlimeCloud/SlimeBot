package com.slimebot.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Util {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("((?<=[a-z])(?=[A-Z]))|((?<=[A-Z])(?=[A-Z][a-z]))");

    public static String[] parseCamelCase(String s) {
        return Arrays.stream(CAMEL_CASE_PATTERN.split(s))
                .map(String::toLowerCase)
                .toArray(String[]::new);
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static boolean isValidURL(String url) {
        if (url == null || url.isBlank()) return false;
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

}
