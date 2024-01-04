package de.slimecloud.slimeball.features.reminder;

import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RemindManager {
	private final ReminderTable table;
	private ScheduledFuture<?> scheduledFuture = null;

	public RemindManager(@NotNull SlimeBot bot) {
		this.table = bot.getReminder();
		scheduleNextReminder();
	}

	public void scheduleNextReminder() {
		if (scheduledFuture != null) scheduledFuture.cancel(true);

		table.getNext().ifPresent(reminder -> {
			long delay = reminder.getTime().toEpochMilli() - System.currentTimeMillis();

			if (delay <= 0) {
				reminder.execute();
				return;
			}

			scheduledFuture = reminder.getBot().getExecutor().schedule(reminder::execute, delay / 1000, TimeUnit.SECONDS);
		});
	}
}
