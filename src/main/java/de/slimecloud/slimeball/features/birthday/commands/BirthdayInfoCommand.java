package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "info", description = "zeigt den Geburtstag eines Mitglieds an")
public class BirthdayInfoCommand {
	@ApplicationCommandMethod
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(name = "target", description = "Der Nutzer, dessen Geburtstag angezeigt werden soll", required = false) Member target
	) {
		event.deferReply().setEphemeral(true).queue();

		Member member = target == null ? event.getMember() : target;

		bot.getBirthdays().get(member).ifPresentOrElse(
				birthday -> event.getHook().editOriginal(":birthday: " + member.getAsMention() + " hat " + TimeFormat.RELATIVE.format(birthday.getNextBirthday()) + " Geburtstag!").queue(),
				() -> event.getHook().editOriginal(":x: Ich kenne " + member.getAsMention() + "'s Geburtstag noch nicht.").queue()
		);
	}
}
