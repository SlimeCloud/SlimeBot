package com.slimebot.commands.report;

import com.slimebot.main.Main;
import com.slimebot.report.Report;
import com.slimebot.report.Status;
import com.slimebot.utils.Config;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.Choice;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.events.interaction.SelectionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationCommand(name = "list", description = "Zeigt alle Meldungen an")
public class ListCommand {
	@WhenFinished
	public void setup(DiscordUtils manager) {
		manager.getEventManager().registerHandler(new SelectionHandler<>(StringSelectInteractionEvent.class, "report:details", event -> {
			String id = event.getValues().get(0);
			Report report = Report.get(event.getGuild().getId(), Integer.parseInt(id));

			MessageEmbed embed = report.asEmbed(event.getGuild().getId());

			if(report.status == Status.CLOSED) {
				event.replyEmbeds(embed).setEphemeral(true).queue();
			}

			else {
				event.replyEmbeds(embed).setActionRow(Report.closeButton(report.id)).setEphemeral(true).queue();
			}
		}));
	}

	public final List<Choice> statusChoices = Arrays.asList(
			new Choice("Alle", "all"),
			new Choice("Geschlossen", "closed"),
			new Choice("Offen", "open")
	);

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(name = "status", description = "Der Status, nach dem gefiltert werden soll", choices = "statusChoices") String status
	) {
		//TODO Rework this as soon as we use a database for storing these information

		EmbedBuilder embed = new EmbedBuilder()
				.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
				.setDescription("Nutze /report_detail oder das Dropdown menu um mehr infos zu einem Report zu bekommen.")
				.setColor(Main.embedColor(event.getGuild().getId()));

		ArrayList<Integer> ReportIdList = new ArrayList<>();
		int fieldSize = 0;
		boolean maxFieldSize = false;

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

		switch(event.getOption("status").getAsString()) {
			case "all" -> {
				embed.setTitle("Eine Liste aller Reports");
				for(Report report : allReports) {
					ReportIdList.add(report.id);
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
				for(Report report : allReports) {
					if(!(report.status == Status.CLOSED)) {
						continue;
					}
					ReportIdList.add(report.id);
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
				for(Report report : allReports) {
					if(!(report.status == Status.OPEN)) {
						continue;
					}
					ReportIdList.add(report.id);
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
					.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
					.setColor(Main.embedColor(event.getGuild().getId()))
					.setTitle(":exclamation: Error: No Reports Found")
					.setDescription("Es wurden keine Reports zu der Ausgewählten option (" + event.getOption("status").getAsString() + ") gefunden!");

			event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
			return;
		}

		if(maxFieldSize) {
			embed.setFooter("ERROR Weitere Reports gefunden - Fehler beim laden - com.slimebot.report.commands.ReportList:86");
		}

		MessageEmbed ed = embed.build();

		event.replyEmbeds(ed).addActionRow(DetailDropdownButton(ReportIdList)).setEphemeral(true).queue();
	}

	private void addReportField(Report report, EmbedBuilder embed) {
		embed.addField("Report #" + report.id,
				report.user.getAsMention() + " wurde am ` " + report.time.format(Main.dtf) + "` von " + report.by.getAsMention() + " gemeldet.",
				false
		);
	}

	private StringSelectMenu DetailDropdownButton(ArrayList<Integer> reportList) {
		StringSelectMenu.Builder btnBuilder = StringSelectMenu.create("report:details")
				.setPlaceholder("Details zu einem Report")
				.setMaxValues(1);

		for(Integer reportID : reportList) {
			btnBuilder.addOption("Report #" + reportID, reportID.toString(), "Details zum Report #" + reportID);
		}

		return btnBuilder.build();
	}
}
