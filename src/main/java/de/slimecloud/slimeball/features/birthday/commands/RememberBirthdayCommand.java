package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
	                           @Option(name = "month", description = "der Monat in dem du Geburtstag hast") Month month
	) {
		try {
			ZonedDateTime date = LocalDateTime.of(0, month, day, 0, 0).atZone(Main.timezone);
			bot.getBirthdays().save(event.getMember(), date.toInstant());

			event.getHook().editOriginal(String.format(":birthday: Dein Geburtstag wurde auf den `" + formatter.format(date) + "` gesetzt.", date)).queue();
		} catch (DateTimeException e) {
			event.getHook().editOriginal(":x: " + e.getMessage()).queue();
		}
	}
}
