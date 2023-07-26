package com.slimebot.util;

public class MathUtil {

    public static double round(double value, int places) {
        int scale = (int) Math.pow(10, places);
        return (double) Math.round(value * scale) / scale;
    }

    public static int round(double value) {
        return (int) round(value, 0);
    }

    public static double range(double d1, double d2) {
        return Math.max(d1, d2) - Math.min(d1, d2);
    }

    public static double randomDouble(double min, double max) {
        return Math.random() * range(min, max) + min;
    }

    public static int randomInt(int min, int max) {
        return (int) (Math.random() * range(min, max) + min);
    }

}
