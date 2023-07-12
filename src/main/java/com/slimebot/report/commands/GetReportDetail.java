package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

public class GetReportDetail extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(!event.getName().equals("report_detail")) return;

		if(Blockreport.isBlocked(event.getMember())) {
			EmbedBuilder noTeam = new EmbedBuilder()
					.setTimestamp(Instant.now())
					.setColor(Main.database.getColor(event.getGuild()))
					.setTitle(":exclamation: Error")
					.setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");

			event.replyEmbeds(noTeam.build()).queue();
			return;
		}

		Report.get(event.getGuild(), event.getOption("id").getAsInt())
				.ifPresentOrElse(
						report -> event.reply(report.buildMessage()).setEphemeral(true).queue(),
						() -> event.reply("Report nicht gefunden").setEphemeral(true).queue()
				);
	}
}
