package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;

@ApplicationCommand(name = "remember", description = "Speichere dein Geburtstag", scope = Scope.GUILD_GLOBAL, defer = true)
public class RememberBirthdayCommand {

	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @Option(name = "day", description = "der tag an dem du geburtstag hast", minValue = 1, maxValue = 31) Integer day, @Option(name = "month", description = "der monat in dem du geburtstag hast") Month month, @Option(name = "year", description = "das jahr in dem du geburtstag hast", minValue = 1900, maxValue = 2024, required = false) Integer year) {
		try {
			ZonedDateTime dateTime = LocalDateTime.of(year==null ? 0 : year, month, day, 0, 0).atZone(ZoneId.systemDefault());
			bot.getBirthdayTable().set(event.getMember(), dateTime.toInstant());

			String date = year==null ? String.format("`%s`", dateTime.format(DateTimeFormatter.ofPattern("dd.MM"))) : TimeFormat.DATE_SHORT.format(dateTime);

			event.getHook().editOriginal(String.format(":birthday: Dein Geburtstag wurde auf den %s gesetzt", date)).queue();
		} catch (DateTimeException e) {
			event.getHook().editOriginal(":x: " + e.getMessage()).queue();
		}
	}

}
