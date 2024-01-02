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

@ApplicationCommand(name = "birthday", description = "zeigt dein Geburtstag, oder den des angegebenen Nutzers am", scope = Scope.GUILD_GLOBAL)
public class BirthdayInfoCommand {

	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event, @Option(name = "target", description = "Der Nutzer, dessen Geburtstag angezeigt werden soll", required = false) Member target) {
		if (target==null) target = event.getMember();
		event.deferReply().queue();

		Birthday birthday = bot.getBirthdayTable().get(target).orElse(null);

		EmbedBuilder builder = new EmbedBuilder();

		if (birthday==null) {
			builder.setDescription(String.format("Ich kenne %S's Geburtstag noch nicht.", target.getAsMention()));
			builder.setColor(new Color(0xDD2222));
		} else {
			builder.setDescription(String.format("%s hat in %s geburtstag!", target.getAsMention(), TimeFormat.RELATIVE.format(birthday.getDate())));
			builder.setColor(bot.getColor(event.getGuild()));
		}

		event.getHook().editOriginalEmbeds(builder.build()).queue();

	}

}
