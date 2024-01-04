package de.slimecloud.slimeball.features.reminder;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

@ApplicationCommand(name = "remindme", description = "Setzt einen Reminder")
public class RemindMeCommand {
	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Die Zeit an welcher du erinnert werden möchtest, beispielsweise 13:30") String time,
	                           @Option(description = "Die Sache an die du erinnert werden möchtest") String message
	) {
		try {
			LocalDateTime now = LocalDateTime.now(Main.timezone);

			Instant timestamp = LocalDateTime.parse(time, new DateTimeFormatterBuilder().appendPattern("HH:mm[ dd.MM.yyyy]")
					.parseDefaulting(ChronoField.DAY_OF_MONTH, now.getDayOfMonth())
					.parseDefaulting(ChronoField.MONTH_OF_YEAR, now.getMonthValue())
					.parseDefaulting(ChronoField.YEAR, now.getYear())
					.toFormatter()
			).toInstant(Main.timezone);

			if (timestamp.isBefore(Instant.now())) {
				event.reply("Deine angegebene Zeit ist schon vergangen!").setEphemeral(true).queue();
				return;
			}

			bot.getReminder().createReminder(event.getMember(), timestamp, message);

			event.reply("Ich erinnere dich " + TimeFormat.RELATIVE.format(timestamp)).setEphemeral(true).queue();
		} catch (DateTimeParseException e) {
			e.printStackTrace();
			event.reply("Falsches Zeitformat! Versuche etwas wie \"14:45\" oder \"09:04 04.05.2024\"").setEphemeral(true).queue();
		}
	}
}

