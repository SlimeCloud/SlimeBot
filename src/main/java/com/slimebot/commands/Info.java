package com.slimebot.commands;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

public class Info extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(!event.getFullCommandName().equals("info")) return;

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("Informationen über den SlimeBall bot")
				.setColor(Main.database.getColor(event.getGuild()))
				.setTimestamp(Instant.now())
				.setDescription("Dieser Bot ist ein Custom bot des SlimeCloud Discords und stellt Features bereit die so von keinem anderen Bot gelöst werden können.")
				.addField("Gecodet von:", "[SlimeCloud DevTeam](https://github.com/SlimeCloud)", true)
				.addField("Version:", Main.version, true)
				.addField("Support:", "Bei Fragen, Verbesserungen, Bugs öffne ein Ticket", true)
				.addField("Prefix:", "Dieser Bot nutzt Slash Commands", true)
				.setFooter("SlimeBall", "https://media.discordapp.net/attachments/1098639892608712714/1098639949592539166/SlimeBall.png");

		event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
	}
}
