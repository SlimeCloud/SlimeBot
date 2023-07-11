package com.slimebot.commands;


import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Ping extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if(!event.getName().equals("ping")) return;

		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("Pong")
				.setDescription(event.getJDA().getGatewayPing() + "ms")
				.setColor(Main.database.getColor(event.getGuild()));

		event.replyEmbeds(embed.build()).setEphemeral(true).queue();
	}
}
