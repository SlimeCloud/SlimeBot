package com.slimebot.util;

import java.util.Random;

public class MathUtil {
    public final static Random random = new Random();

    public static double round(double value, int places) {
        int scale = (int) Math.pow(10, places);
        return (double) Math.round(value * scale) / scale;
    }

    public static int round(double value) {
        return (int) round(value, 0);
    }

    public static int randomInt(int lowerBound, int upperBound) {
        return lowerBound + random.nextInt(upperBound - lowerBound);
    }

    public static double randomDouble(double lowerBound, double upperBound) {
        return lowerBound + random.nextDouble(upperBound - lowerBound);
    }
}
