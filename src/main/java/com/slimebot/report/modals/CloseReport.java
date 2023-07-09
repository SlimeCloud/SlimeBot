package com.slimebot.report.modals;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

public class CloseReport extends ListenerAdapter {
	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if(!event.getModalId().equals("close")) return;

		String reasonInput = event.getValue("reason").getAsString();
		int reportID = Integer.parseInt(event.getValue("id").getAsString());
		boolean reportFound = false;

		YamlFile reportFile = Config.getConfig(event.getGuild().getId(), "reports");
		try {
			reportFile.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		ConfigurationSection reportSection = reportFile.getConfigurationSection("reports");
		ArrayList<Report> allReports = new ArrayList<>();
		for(int id = 2; id <= reportSection.size(); id++) {
			allReports.add(Report.get(event.getGuild().getId(), id));
		}

		for(Report report : allReports) {
			if(!(Objects.equals(report.id, reportID))) {
				continue;
			}

			reportFound = true;
			report.closeReason = reasonInput;
			report.status = Status.CLOSED;
			Report.save(event.getGuild().getId(), report);
		}

		if(!reportFound) {
			EmbedBuilder noReport = new EmbedBuilder()
					.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
					.setColor(Main.embedColor(event.getGuild().getId()))
					.setTitle(":exclamation: Error: Report not Found")
					.setDescription("Der Report #" + reportID + " konnte nicht gefunden werden!");
			event.replyEmbeds(noReport.build()).setEphemeral(true).queue();
			return;
		}

		EmbedBuilder embed = new EmbedBuilder()
				.setColor(Main.embedColor(event.getGuild().getId()))
				.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
				.setTitle("Report **#" + reportID + "** closed")
				.setDescription("Der Report mit der ID **#" + reportID + "** wurde erfolgreich geschlossen");

		event.replyEmbeds(embed.build()).queue();


	}
}
