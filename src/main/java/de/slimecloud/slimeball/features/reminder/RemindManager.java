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

			scheduleNextReminder();
		}
	}

	public void scheduleNextReminder() {
		Optional<Reminder> reminderOptional = table.getNext();
		if(reminderOptional.isPresent()) {
			if(scheduledFuture != null)scheduledFuture.cancel(true);
			Reminder reminder = reminderOptional.get();
			long delay = reminder.getTime() - LocalDateTime.now().atZone(ZoneOffset.systemDefault()).toInstant().getEpochSecond();
			scheduledFuture = reminder.getBot().getExecutor().schedule(
					() -> {
						reminder.getBot().getJda().retrieveUserById(reminder.getUser().getIdLong()).queue(user -> {
							user.openPrivateChannel()
									.flatMap(channel -> channel.sendMessage(reminder.getMessage()))
									.queue();

						});
					}, delay, TimeUnit.SECONDS);
		}
	}
}
