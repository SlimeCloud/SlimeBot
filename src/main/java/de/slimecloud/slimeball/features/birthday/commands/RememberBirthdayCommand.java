package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationCommand(name = "remember", description = "Speichert deinen Geburtstag", defer = true)
public class RememberBirthdayCommand {
	public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(name = "day", description = "der Tag an dem du Geburtstag hast", minValue = 1, maxValue = 31) int day,
	                           @Option(name = "month", description = "der Monat in dem du Geburtstag hast") Month month,
	                           @Option(name = "year", description = "das jahr in dem du geburtstag hast", minValue = 1900, maxValue = 2024, required = false) Integer year
	) {
		try {
			ZonedDateTime date = LocalDateTime.of(year == null ? 0 : year, month, day, 0, 0).atZone(SlimeBot.timezone);
			bot.getBirthdays().save(event.getMember(), date.toInstant());

			String dateString = year == null
					? "`" + formatter.format(date) + "`"
					: TimeFormat.DATE_SHORT.format(date);

			event.getHook().editOriginal(String.format(":birthday: Dein Geburtstag wurde auf den " + dateString + " gesetzt", date)).queue();
		} catch (DateTimeException e) {
			event.getHook().editOriginal(":x: " + e.getMessage()).queue();
		}
	}
}
