package com.slimebot.report.commands;

import com.slimebot.main.Checks;
import com.slimebot.main.Main;
import com.slimebot.report.assets.Filter;
import com.slimebot.report.assets.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ReportList extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(!event.getName().equals("report_list")) return;

		if(Checks.hasTeamRole(event.getMember())) {
			EmbedBuilder noTeam = new EmbedBuilder()
					.setTimestamp(Instant.now())
					.setColor(Main.database.getColor(event.getGuild()))
					.setTitle(":exclamation: Error")
					.setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");
			event.replyEmbeds(noTeam.build()).setEphemeral(true).queue();
			return;
		}

		Filter filter = Filter.valueOf(event.getOption("status").getAsString().toUpperCase());

		EmbedBuilder embed = new EmbedBuilder()
				.setTimestamp(Instant.now())
				.setDescription("Nutze /report_detail oder das Dropdown menu um mehr infos zu einem Report zu bekommen.")
				.setColor(Main.database.getColor(event.getGuild()));

		List<Report> reports = Main.database.handle(handle -> handle.createQuery("select * from reports where guild = :guild")
				.bind("guild", event.getGuild())
				.mapTo(Report.class)
				.stream()
				.filter(filter.filter)
				.toList()
		);

		if(reports.isEmpty()) {
			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setTimestamp(Instant.now())
					.setColor(Main.database.getColor(event.getGuild()))
					.setTitle(":exclamation: Error: No Reports Found")
					.setDescription("Es wurden keine Reports zu der Ausgewählten option (" + event.getOption("status").getAsString() + ") gefunden!");

			event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
		}

		for(int i = 0; i < reports.size() && i < 24; i++) {
			Report report = reports.get(i);

			embed.addField("Report #" + report.getId(),
					report.shortDescription(),
					false
			);
		}

		if(reports.size() > 24) {
			embed.setFooter("Weitere Reports gefunden, es können jedoch maximal 25 angezeigt werden");
		}

		ArrayList<Integer> ReportIdList = new ArrayList<>();
		int fieldSize = 0;
		boolean maxFieldSize = false;

		switch(event.getOption("status").getAsString()) {
			case "all" -> {
				embed.setTitle("Eine Liste aller Reports");
				for(Report report : reports) {
					ReportIdList.add(report.getId());
					if(fieldSize > 24) {
						maxFieldSize = true;
						break;
					}
					addReportField(report, embed);
					fieldSize++;
				}
			}
			case "closed" -> {
				embed.setTitle("Eine Liste aller geschlossenen Reports");
				for(Report report : reports) {
					if(report.isOpen()) continue;

					ReportIdList.add(report.getId());
					if(fieldSize > 24) {
						maxFieldSize = true;
						break;
					}
					addReportField(report, embed);
					fieldSize++;
				}
			}
			case "open" -> {
				embed.setTitle("Eine Liste aller offenen Reports");
				for(Report report : reports) {
					if(!report.isOpen()) continue;

					ReportIdList.add(report.getId());
					if(fieldSize > 24) {
						maxFieldSize = true;
						break;
					}
					addReportField(report, embed);
					fieldSize++;
				}
			}

		}

		if(ReportIdList.size() == 0) {
			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setTimestamp(Instant.now())
					.setColor(Main.database.getColor(event.getGuild()))
					.setTitle(":exclamation: Error: No Reports Found")
					.setDescription("Es wurden keine Reports zu der Ausgewählten option (" + event.getOption("status").getAsString() + ") gefunden!");

			event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
			return;
		}

		if(maxFieldSize) {
			embed.setFooter("ERROR Weitere Reports gefunden - Fehler beim laden - com.slimebot.report.commands.ReportList:86");
		}

		MessageEmbed ed = embed.build();

		event.replyEmbeds(ed).addActionRow(DetailDropdownButton(ReportIdList)).queue();
	}

	private void addReportField(Report report, EmbedBuilder embed) {
		embed.addField("Report #" + report.getId(),
				report.shortDescription(),
				false
		);
	}

	private StringSelectMenu DetailDropdownButton(List<Integer> reportList) {
		StringSelectMenu.Builder btnBuilder = StringSelectMenu.create("detail_btn")
				.setPlaceholder("Details zu einem Report")
				.setMaxValues(1);

		for(int reportID : reportList) {
			btnBuilder.addOption("Report #" + reportID, String.valueOf(reportID), "Details zum Report #" + reportID);
		}

		return btnBuilder.build();
	}
}
