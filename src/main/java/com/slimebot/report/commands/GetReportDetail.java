package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import com.slimebot.utils.Checks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class GetReportDetail extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(!event.getName().equals("report_detail")) return;

		if(Checks.hasTeamRole(event.getMember(), event.getGuild())) {
			EmbedBuilder noTeam = new EmbedBuilder()
					.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
					.setColor(Main.embedColor(event.getGuild().getId()))
					.setTitle(":exclamation: Error")
					.setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");

			event.replyEmbeds(noTeam.build()).queue();
			return;
		}

		OptionMapping id = event.getOption("id");
		Report report = Report.get(event.getGuild().getId(), id.getAsInt());

		MessageEmbed embed = report.asEmbed(event.getGuild().getId());
		Button closeBtn = Report.closeButton(report.id);

		if(report.status == Status.CLOSED) {
			event.replyEmbeds(embed).queue();
		}
		else {
			event.replyEmbeds(embed).setActionRow(closeBtn).queue();
		}
	}
}
