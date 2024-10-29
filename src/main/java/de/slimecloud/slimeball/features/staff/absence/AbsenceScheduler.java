package de.slimecloud.slimeball.features.staff.absence;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Slf4j
public class AbsenceScheduler {
	private final SlimeBot bot;

	public AbsenceScheduler(@NotNull SlimeBot bot) {
		this.bot = bot;

		bot.getScheduler().scheduleDaily(12, true, this::check);
	}

	private void check() {
		logger.info("Checking Absences...");

		List<Absence> expired = bot.getAbsences().getExpiredAbsences();
		logger.info("Found {} expired absences", expired.size());

		List<Absence> starting = bot.getAbsences().getStartingAbsences();
		logger.info("Found {} starting absences", starting.size());

		expired.forEach(Absence::delete);
		starting.forEach(Absence::start);
	}
}
