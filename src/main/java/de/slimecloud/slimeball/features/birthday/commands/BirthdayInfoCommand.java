package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.birthday.Birthday;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

@ApplicationCommand(name = "info", description = "zeigt dein Geburtstag, oder den des angegebenen Nutzers am")
public class BirthdayInfoCommand {

	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @Option(name = "target", description = "Der Nutzer, dessen Geburtstag angezeigt werden soll", required = false) Member target) {
		if (target == null) target = event.getMember();
		event.deferReply().queue();

		Birthday birthday = bot.getBirthdayTable().get(target).orElse(null);

		if (birthday == null) event.getHook().editOriginal(String.format(":x: Ich kenne %s's Geburtstag noch nicht.", target.getAsMention())).queue();
		else {
			ZonedDateTime zdt = birthday.getNextBirthday();
			String format = TimeFormat.RELATIVE.format(zdt);
			event.getHook().editOriginal(String.format(":birthday: %s hat %s Geburtstag!", target.getAsMention(), format)).queue();
		}


	}

}
