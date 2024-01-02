package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.birthday.Birthday;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationCommand(name = "info", description = "zeigt dein Geburtstag, oder den des angegebenen Nutzers am", scope = Scope.GUILD_GLOBAL)
public class BirthdayInfoCommand {

	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @Option(name = "target", description = "Der Nutzer, dessen Geburtstag angezeigt werden soll", required = false) Member target) {
		if (target==null) target = event.getMember();
		event.deferReply().queue();

		Birthday birthday = bot.getBirthdayTable().get(target).orElse(null);

		if (birthday==null) {
			event.getHook().editOriginal(String.format(":x: Ich kenne %S's Geburtstag noch nicht.", target.getAsMention())).queue();
		} else {
			ZonedDateTime zdt = birthday.getInstant().atZone(ZoneId.systemDefault());
			String format = zdt.getYear()==0 ? zdt.format(DateTimeFormatter.ofPattern("d/M")) : TimeFormat.RELATIVE.format(birthday.getInstant());
			event.getHook().editOriginal(String.format(":birthday: %s hat in %s geburtstag!", target.getAsMention(), format)).queue();
		}



	}

}
