package de.slimecloud.slimeball.features.birthday.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "info", description = "zeigt den Geburtstag eines Mitglieds an", defer = true)
public class BirthdayInfoCommand {
	@ApplicationCommandMethod
	@SuppressWarnings("ConstantConditions")
	public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
	                           @Option(name = "target", description = "Der Nutzer, dessen Geburtstag angezeigt werden soll", required = false) Member target
	) {
		Member member = target == null ? event.getMember() : target;

		bot.getBirthdays().get(member).ifPresentOrElse(
				birthday -> {
					int age = birthday.getAge();
					event.getHook().editOriginal(":birthday: " + member.getAsMention() + " hat " + birthday.getFormat() + " Geburtstag" + (age == -1 ? "!" : String.format(" und wird %s Jahre alt!", ++age))).queue();
				},
				() -> event.getHook().editOriginal(":x: Ich kenne " + member.getAsMention() + "'s Geburtstag noch nicht.").queue()
		);
	}
}
