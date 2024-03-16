package de.slimecloud.slimeball.features.reminder;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.list.ListManager;
import de.slimecloud.slimeball.main.CommandPermission;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;

@ApplicationCommand(name = "remind", description = "Setzt einen Reminder")
public class RemindCommand {
	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull ListManager<ICommandContext> manager, @NotNull Command<ICommandContext> command) {
		command.addSubcommand(manager.createCommand(s -> bot.getReminder()).withDescription("Zeigt alle deine Reminder an"));
	}

	public static void createReminder(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                                  @Nullable Role role,
	                                  @NotNull String time,
	                                  @NotNull String message
	) throws DateTimeParseException {
		if (message.length() > 1024) event.reply("Deine Nachricht darf maximal nur 1024 Zeichen lang sein!").setEphemeral(true).queue();
		Instant timestamp = convertTime(time);

		if (timestamp.isBefore(Instant.now())) event.reply("Deine angegebene Zeit ist schon vergangen!").setEphemeral(true).queue();

		try {
			bot.getReminder().createReminder(event.getMember(), role, timestamp, Instant.now(), message);
			event.reply("Reminder wurde gesetzt! Löst aus " + TimeFormat.RELATIVE.format(timestamp)).setEphemeral(true).queue();
		} catch (DateTimeParseException e) {
			event.reply("Falsches Zeitformat! Versuche etwas wie \"14:45\" oder \"09:04 04.05.2024\"").setEphemeral(true).queue();
		}
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
			createReminder(bot, event, null, time, message);
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
			createReminder(bot, event, role, time, message);
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

			if (number < 1 || number > reminders.size()) {
				event.reply("Diesen Reminder gibt es nicht oder ist bereits ausgelaufen!").setEphemeral(true).queue();
				return;
			}

			reminders.get(number - 1).delete();
			bot.getRemindManager().scheduleNextReminder();

			event.reply("Reminder gelöscht!").setEphemeral(true).queue();
		}
	}
}