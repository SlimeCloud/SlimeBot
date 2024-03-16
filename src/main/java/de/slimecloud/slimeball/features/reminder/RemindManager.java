package de.slimecloud.slimeball.features.reminder;

import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;

public class RemindManager {
	private final ReminderTable table;
	private ScheduledFuture<?> scheduledFuture = null;

	public RemindManager(@NotNull SlimeBot bot) {
		this.table = bot.getReminder();
	}

	public void scheduleNextReminder() {
		if (scheduledFuture != null) scheduledFuture.cancel(true);
		table.getNext().flatMap(Reminder::schedule).ifPresent(f -> scheduledFuture = f);
	}
}