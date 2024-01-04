package de.slimecloud.slimeball.features.reminder;

import de.slimecloud.slimeball.main.SlimeBot;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RemindManager {
	private ReminderTable table;
	private ScheduledFuture<?> scheduledFuture = null;

	public RemindManager(@NotNull SlimeBot bot) {
		if(bot.getReminder() != null) {
			table = bot.getReminder();

			// Schedule the next reminder after the Bot starts
			scheduleNextReminder();
		}
	}

	public void scheduleNextReminder() {
		Optional<Reminder> reminderOptional = table.getNext();
		if(reminderOptional.isPresent()) {
			Reminder reminder = reminderOptional.get();
			long delay = reminder.getTime() - LocalDateTime.now().atZone(ZoneOffset.systemDefault()).toInstant().getEpochSecond();

			if(delay<= 0) {
				System.out.println(reminder.getId());
				reminder.execute();
				return;
			}

			if(scheduledFuture != null)scheduledFuture.cancel(true);


			scheduledFuture = reminder.getBot().getExecutor().schedule(
					reminder::execute, delay, TimeUnit.SECONDS);
		}
	}
}
