package com.slimebot.report.commands;

import com.slimebot.main.Checks;
import com.slimebot.main.Main;
import com.slimebot.report.assets.Filter;
import com.slimebot.report.assets.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;
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
				.bind("guild", event.getGuild().getIdLong())
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

		StringSelectMenu.Builder select = StringSelectMenu.create("report:details")
				.setPlaceholder("Details zu einem Report")
				.setMaxValues(1);

		for(int i = 0; i < reports.size() && i < 24; i++) {
			Report report = reports.get(i);

			embed.addField("Report #" + report.getId(),
					report.shortDescription(),
					false
			);

			select.addOption("Report #" + report.getId(), String.valueOf(report.getId()), "Details zum Report #" + report.getId());
		}

		if(reports.size() > 24) {
			embed.setFooter("Weitere Reports gefunden, es können jedoch maximal 25 angezeigt werden");
		}

		event.replyEmbeds(embed.build()).addActionRow(select.build()).queue();
	}
}
