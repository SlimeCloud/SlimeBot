package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class ReportCmd extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if(!event.getName().equals("report")) return;

		if(Blockreport.isBlocked(event.getMember())) {
			event.replyEmbeds(
					new EmbedBuilder()
							.setTimestamp(Instant.now())
							.setColor(Main.database.getColor(event.getGuild()))
							.setTitle(":exclamation: Error: Blocked")
							.setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst")
							.build()
			).setEphemeral(true).queue();
			return;
		}

		Report report = Report.createReport(event.getGuild(), Type.USER, event.getUser(), event.getOption("user").getAsUser(), event.getOption("beschreibung").getAsString());

		event.replyEmbeds(
				new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(Main.database.getColor(event.getGuild()))
						.setTitle(":white_check_mark: Report Erfolgreich")
						.setDescription(event.getOption("user").getAsUser().getAsMention() + " wurde erfolgreich gemeldet")
						.build()
		).setEphemeral(true).queue();

		report.log();
	}
}
