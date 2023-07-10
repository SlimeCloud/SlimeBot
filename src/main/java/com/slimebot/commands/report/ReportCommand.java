package com.slimebot.commands.report;

import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.report.Report;
import com.slimebot.report.Status;
import com.slimebot.utils.Config;
import de.mineking.discord.DiscordUtils;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.events.interaction.ButtonHandler;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.Instant;

@ApplicationCommand(name = "report", description = "Verwaltet reports", subcommands = {BlockCommand.class, DetailsCommand.class, ListCommand.class})
public class ReportCommand {
	public final CommandPermission permission = CommandPermission.TEAM;

	@WhenFinished
	public void setup(DiscordUtils manager) {
		manager.getEventManager().registerHandler(new ButtonHandler("report:close", event -> {
			String reportID = event.getButton().getLabel().split("#")[1];

			event.replyModal(
					Modal.create("report:close", "Meldung schließen")
							.addActionRow(
									TextInput.create("reason", "Wie Wurde mit dem Report Verfahren?", TextInputStyle.SHORT)
											.setRequired(true)
											.setPlaceholder("z. B. Warn, Kick, Mute, Ban, Nichts etc..")
											.build()
							)
							.addActionRow(
									TextInput.create("id", "ID des Reports der geschlossen wird", TextInputStyle.SHORT)
											.setRequired(true)
											.setValue(reportID)
											.setPlaceholder("Dieses Feld wird automatisch ausgefüllt!")
											.build()
							)
							.build()
			).queue();
		}));

		manager.getEventManager().registerHandler(new ModalHandler("report:close", event -> {
			YamlFile reportFile = Config.getConfig(event.getGuild().getId(), "reports");
			try {
				reportFile.load();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			String reason = event.getValue("reason").getAsString();
			int reportID = Integer.parseInt(event.getValue("id").getAsString());

			Report report = Report.get(event.getGuild().getId(), reportID);

			if(report == null) {
				event.replyEmbeds(
						new EmbedBuilder()
								.setTimestamp(Instant.now())
								.setColor(Main.embedColor(event.getGuild().getId()))
								.setTitle(":exclamation: Error: Report not Found")
								.setDescription("Der Report #" + reportID + " konnte nicht gefunden werden!")
								.build()
				).setEphemeral(true).queue();
				return;
			}

			report.closeReason = reason;
			report.status = Status.CLOSED;
			Report.save(event.getGuild().getId(), report);

			event.replyEmbeds(
					new EmbedBuilder()
							.setColor(Main.embedColor(event.getGuild().getId()))
							.setTimestamp(Instant.now())
							.setTitle("Report **#" + reportID + "** geschlossen")
							.setDescription("Der Report mit der ID **#" + reportID + "** wurde erfolgreich geschlossen")
							.build()
			).queue();
		}));
	}
}
