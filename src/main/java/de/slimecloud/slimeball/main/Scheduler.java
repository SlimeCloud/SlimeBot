package de.slimecloud.slimeball.main;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
	private final Map<Integer, Set<Runnable>> dailyActions = new HashMap<>();

	public Scheduler(ScheduledExecutorService executor) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = now.plusHours(1).truncatedTo(ChronoUnit.HOURS).plusSeconds(10); //We cannot use the pure full hour because that will cause LocalDateTime.now().getHour() to be still the last hour and therefore failing

		executor.scheduleAtFixedRate(
				() -> dailyActions.getOrDefault(ZonedDateTime.now(Main.timezone).getHour(), Collections.emptySet()).forEach(Runnable::run),
				Duration.between(now, start).getSeconds(),
				TimeUnit.HOURS.toSeconds(1),
				TimeUnit.SECONDS
		);
	}

	public void scheduleDaily(int hour, boolean run, @NotNull Runnable action) {
		if (run && LocalDateTime.now().getHour() >= hour) action.run();
		dailyActions.computeIfAbsent(hour, k -> new HashSet<>()).add(action);
	}

	public void scheduleDaily(int hour, @NotNull Runnable action) {
		scheduleDaily(hour, false, action);
	}
}
