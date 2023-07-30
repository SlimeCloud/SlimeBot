package com.slimebot.commands.level.reset;

import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "all", description = "Setzt alle Statistiken eines Nutzers zurück")
public class ResetAllCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
							   @Option(name = "member") Member member
	) {
		event.reply(
				ResetLevelCommand.execute(member) + "\n\n" +
						ResetMessagesCommand.execute(member)
		).setEphemeral(true).queue();
	}
}