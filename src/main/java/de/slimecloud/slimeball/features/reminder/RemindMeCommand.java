package de.slimecloud.slimeball.features.reminder;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@ApplicationCommand(name = "remindme", description = "Set a reminder")
public class RemindMeCommand {

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(description = "Die Zeit an welcher du erinnert werden möchtest, beispielsweise 13:30") String time,
	                           @Option(description = "Die Sache an die du erinnert werden möchtest") String message
	) {
		if(time == null || message == null) {
			event.reply("ERROR").setEphemeral(true).queue();
			return;
		}

		// convert date
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime dateTime = LocalDateTime.now();
		try {
			dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time, formatter));
		} catch (DateTimeParseException e1) {
			try {
				formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
				dateTime = LocalDateTime.parse(time, formatter);
			} catch (DateTimeParseException e2) {
				event.reply("Falsches Zeitformat! Versuche etwas wie \"14:45\" oder \"09:04 04.05.2024\"").setEphemeral(true).queue();
				return;
			}
		}
		ZonedDateTime zonedDateTime = dateTime.atZone(ZoneOffset.systemDefault());

		long unixTime = zonedDateTime.toInstant().getEpochSecond();


		// Check if the time has already elapsed
		long unixTimeNow = LocalDateTime.now().atZone(ZoneOffset.systemDefault()).toInstant().getEpochSecond();

		if(unixTime-unixTimeNow <= 0) {
			event.reply("Deine angegebene Zeit ist schon vergangen!").setEphemeral(true).queue();
			return;
		}

		bot.getReminder().createReminder(event.getGuild(), event.getUser(), unixTime, message);
		// Schedule the next reminder
		bot.getRemindManager().scheduleNextReminder();

		event.reply(String.valueOf(unixTime)).queue();
	}
}

