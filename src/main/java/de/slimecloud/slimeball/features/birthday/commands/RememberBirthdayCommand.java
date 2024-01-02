package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;

@ApplicationCommand(name = "remember-birthday", description = "Speichere dein Geburtstag", scope = Scope.GUILD_GLOBAL)
public class RememberBirthdayCommand {

	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @Option(name = "day", description = "der tag an dem du geburtstag hast", minValue = 1, maxValue = 31) Integer day, @Option(name = "month", description = "der monat in dem du geburtstag hast") Month month, @Option(name = "year", description = "das jahr in dem du geburtstag hast", minValue = 1900, maxValue = 2024, required = false) Integer year) {
		event.deferReply(true).queue();
		try {
			ZonedDateTime dateTime = LocalDateTime.of(year==null ? 0 : year, month, day, 0, 0).atZone(ZoneId.systemDefault());
			bot.getBirthdayTable().set(event.getMember(), dateTime.toInstant());

			String date = year==null ? dateTime.format(DateTimeFormatter.ofPattern("d/M")) : TimeFormat.DATE_SHORT.format(dateTime);

			MessageEmbed embed = new EmbedBuilder()
					.setColor(bot.getColor(event.getGuild()))
					.setDescription(String.format("Dein geburtstag wurde auf den %s gesetzt", date))
					.build();

			event.getHook().editOriginalEmbeds(embed).queue();
		} catch (DateTimeException e) {
			event.getHook().editOriginal(e.getMessage()).queue();
		}
	}

}
