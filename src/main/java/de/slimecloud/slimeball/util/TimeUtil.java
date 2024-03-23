package de.slimecloud.slimeball.util;

import de.slimecloud.slimeball.main.Main;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZonedDateTime;

public class TimeUtil {
	public static boolean isSameDay(@NotNull Instant first, @NotNull Instant second) {
		return isSameDay(first.atZone(Main.timezone), second.atZone(Main.timezone));
	}

	public static boolean isSameDay(@NotNull ZonedDateTime first, @NotNull ZonedDateTime second) {
		return first.getDayOfMonth() == second.getDayOfMonth() && first.getMonth() == second.getMonth();
	}
}
