package de.slimecloud.slimeball.util;

import de.slimecloud.slimeball.main.Main;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {
	public int round(double value) {
		return (int) (value + 0.5);
	}

	public int randomInt(int lowerBound, int upperBound) {
		return lowerBound == upperBound ? lowerBound : lowerBound + Main.random.nextInt(upperBound - lowerBound);
	}

	public double randomDouble(double lowerBound, double upperBound) {
		return lowerBound == upperBound ? lowerBound : lowerBound + Main.random.nextDouble(upperBound - lowerBound);
	}
}
