package de.slimecloud.slimeball.features.reminder;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;

@ApplicationCommand(name = "remind", description = "Setzt einen Reminder")
public class RemindCommand {

	public static ReplyCallbackAction createReminder(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                                                 Role role,
	                                                 @NotNull String time,
	                                                 @NotNull String message
	) throws DateTimeParseException {
		if (message.length() > 1024) {
			return event.reply("Deine Message darf maximal nur 1024 Zeichen lang sein!");
		}
		Instant timestamp = convertTime(time);

		if (timestamp.isBefore(Instant.now())) {
			return event.reply("Deine angegebene Zeit ist schon vergangen!");
		}

		bot.getReminder().createReminder(event.getMember(), role, timestamp, LocalDateTime.now(Main.timezone).toInstant(ZoneOffset.UTC), message);

		return event.reply("Reminder wurde gesetzt! Löst aus " + TimeFormat.RELATIVE.format(timestamp));
	}

	public static Instant convertTime(String time) throws DateTimeParseException {
		LocalDateTime now = LocalDateTime.now(Main.timezone);

		return LocalDateTime.parse(time, new DateTimeFormatterBuilder().appendPattern("HH:mm[ dd.MM.yyyy]")
				.parseDefaulting(ChronoField.DAY_OF_MONTH, now.getDayOfMonth())
				.parseDefaulting(ChronoField.MONTH_OF_YEAR, now.getMonthValue())
				.parseDefaulting(ChronoField.YEAR, now.getYear())
				.toFormatter()
		).toInstant(Main.timezone);
	}

	@ApplicationCommand(name = "me", description = "Setze einen Reminder")
	public static class MeCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Die Zeit an welcher du erinnert werden möchtest, beispielsweise 13:30") String time,
		                           @Option(description = "Die Sache an die du erinnert werden möchtest") String message
		) {
			try {
				createReminder(bot, event, null, time, message).setEphemeral(true).queue();
			} catch (DateTimeParseException e) {
				e.printStackTrace();
				event.reply("Falsches Zeitformat! Versuche etwas wie \"14:45\" oder \"09:04 04.05.2024\"").setEphemeral(true).queue();
			}
		}
	}

	@ApplicationCommand(name = "role", description = "Setze einen Reminder")
	public static class RoleCommand {
		public final CommandPermission permission = CommandPermission.TEAM;

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Rolle die erwähnt werden soll") Role role,
		                           @Option(description = "Die Zeit an welcher du erinnern möchtest, beispielsweise 13:30") String time,
		                           @Option(description = "Die Sache an die du erinnern möchtest") String message
		) {
			try {
				createReminder(bot, event, role, time, message).setEphemeral(true).queue();
			} catch (DateTimeParseException e) {
				e.printStackTrace();
				event.reply("Falsches Zeitformat! Versuche etwas wie \"14:45\" oder \"09:04 04.05.2024\"").setEphemeral(true).queue();
			}
		}
	}

	@ApplicationCommand(name = "list", description = "Zeige und deine aktiven Reminder")
	public static class ListCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			List<Reminder> reminders = bot.getReminder().getByMember(event.getMember());
			if (reminders.isEmpty()) {
				event.reply("Du hast keine aktiven Reminder auf diesem Server!").setEphemeral(true).queue();
				return;
			}
			EmbedBuilder builder = new EmbedBuilder()
					.setTitle("Reminder")
					.setColor(bot.getColor(event.getGuild()))
					.setFooter("Lösche einen Reminder mit /remind delete [Nummer]");

			for (int i = 0; i < reminders.size(); i++) {
				Reminder reminder = reminders.get(i);
				builder.addField((i + 1) + ": " + TimeFormat.RELATIVE.format(reminder.getTime()), reminder.getMessage(), false);
			}
			event.replyEmbeds(builder.build()).queue();
		}
	}

	@ApplicationCommand(name = "delete", description = "Lösche einen aktiven Reminder")
	public static class DeleteCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Nummer des Reminders") int number
		) {
			List<Reminder> reminders = bot.getReminder().getByMember(event.getMember());
			if (reminders.isEmpty()) {
				event.reply("Du hast keine aktiven Reminder auf diesem Server!").setEphemeral(true).queue();
				return;
			}
			if (number > reminders.size()) {
				event.reply("Diesen Reminder gibt es nicht!").setEphemeral(true).queue();
				return;
			}
			Reminder reminder = reminders.get(number - 1);
			if (reminder != null) {
				reminder.delete();
				bot.getRemindManager().scheduleNextReminder();

				event.reply("Reminder gelöscht!").setEphemeral(true).queue();
			}
		}
	}
}