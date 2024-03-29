package de.slimecloud.slimeball.util;

import de.slimecloud.slimeball.main.Main;

public class MathUtil {
	public static int round(double value) {
		return (int) (value + 0.5);
	}

	public static int randomInt(int lowerBound, int upperBound) {
		return lowerBound==upperBound ? lowerBound : lowerBound + Main.random.nextInt(upperBound - lowerBound);
	}

	public static double randomDouble(double lowerBound, double upperBound) {
		return lowerBound==upperBound ? lowerBound : lowerBound + Main.random.nextDouble(upperBound - lowerBound);
	}
}
