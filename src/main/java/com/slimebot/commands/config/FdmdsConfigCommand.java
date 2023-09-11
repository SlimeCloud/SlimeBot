package com.slimebot.commands.config;

import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FdmdsConfigCommand {
	@ApplicationCommand(name = "disable", description = "Deaktiviert Fdmds für diesen Server")
	public static class DisableCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			ConfigCommand.updateField(event.getGuild(), config -> config.fdmds = null);

			Main.updateGuildCommands(event.getGuild());

			event.reply("Fdmds deaktiviert").setEphemeral(true).queue();
		}
	}
}
