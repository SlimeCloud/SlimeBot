package com.slimebot.commands;


import com.slimebot.main.Main;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "ping", description = "ping pong")
public class PingCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		event.replyEmbeds(
				new EmbedBuilder()
						.setTitle("Pong")
						.setDescription(event.getJDA().getGatewayPing() + "ms")
						.setColor(Main.database.getColor(event.getGuild()))
						.build()
		).setEphemeral(true).queue();
	}
}
