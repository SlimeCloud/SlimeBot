package de.slimecloud.slimeball.util;

import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;

import java.time.Instant;
import java.time.ZonedDateTime;

public class TimeUtil {

	public static boolean isSameDay(Instant instant, Instant other) {
		return isSameDay(instant, other, false);
	}

	public static boolean isSameDay(ZonedDateTime zdt, ZonedDateTime other) {
		return isSameDay(zdt, other, false);
	}

	public static boolean isSameDay(Instant instant, Instant other, boolean ignoreYear) {
		return isSameDay(instant.atZone(Main.timezone), other.atZone(Main.timezone), ignoreYear);
	}

	public static boolean isSameDay(ZonedDateTime zdt, ZonedDateTime other, boolean ignoreYear) {
		return (ignoreYear || zdt.getYear()==other.getYear()) && zdt.getMonth().equals(other.getMonth()) && zdt.getDayOfMonth()==other.getDayOfMonth();
	}

}
