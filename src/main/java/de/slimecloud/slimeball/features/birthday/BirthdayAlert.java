package de.slimecloud.slimeball.features.birthday;

import de.slimecloud.slimeball.features.birthday.event.BirthdayEndEvent;
import de.slimecloud.slimeball.features.birthday.event.BirthdayStartEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BirthdayAlert {

	private final SlimeBot bot;

	public BirthdayAlert(@NotNull SlimeBot bot) {
		this.bot = bot;
		bot.scheduleDaily(6, this::check);
	}


	private void check() {
		List<Birthday> all = bot.getBirthdays().getAll();
		getYesterday(all).forEach(b -> new BirthdayEndEvent(b).callEvent());
		getToday(all).forEach(b -> new BirthdayStartEvent(b).callEvent());
	}


	private List<Birthday> getToday(List<Birthday> all) {
		Instant now = Instant.now();
		return all.stream()
				.filter(b -> TimeUtil.isSameDay(b.getTime(), now, true))
				.toList();
	}

	private List<Birthday> getYesterday(List<Birthday> all) {
		Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
		return all.stream()
				.filter(b -> TimeUtil.isSameDay(b.getTime(), yesterday, true))
				.toList();
	}
}
