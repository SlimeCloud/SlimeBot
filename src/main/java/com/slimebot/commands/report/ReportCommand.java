package com.slimebot.commands.report;

import com.slimebot.main.CommandContext;
import com.slimebot.main.CommandPermission;
import com.slimebot.main.Main;
import com.slimebot.report.Report;
import de.mineking.discord.commands.CommandManager;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.WhenFinished;
import de.mineking.discord.events.Listener;
import de.mineking.discord.events.interaction.ButtonHandler;
import de.mineking.discord.events.interaction.ModalHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.Instant;

@ApplicationCommand(name = "report", description = "Verwaltet reports", guildOnly = true, subcommands = {BlockCommand.class, DetailsCommand.class})
public class ReportCommand {
	public final CommandPermission permission = CommandPermission.TEAM;

	@WhenFinished
	public void setup(CommandManager<CommandContext> cmdMan) {
		cmdMan.registerCommand("report list", new ReportListCommand());
	}

	@Listener(type = ButtonHandler.class, filter = "report:close")
	public void handleCloseButton(ButtonInteractionEvent event) {
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
	}

	@Listener(type = ModalHandler.class, filter = "report:close")
	public void handleCloseModal(ModalInteractionEvent event) {
		int reportID = Integer.parseInt(event.getValue("id").getAsString());

		Report.get(event.getGuild(), reportID)
				.ifPresentOrElse(
						report -> {
							report.close(event.getValue("reason").getAsString());

							event.replyEmbeds(
									new EmbedBuilder()
											.setColor(Main.database.getColor(event.getGuild()))
											.setTimestamp(Instant.now())
											.setTitle("Report **#" + reportID + "** closed")
											.setDescription("Der Report mit der ID **#" + reportID + "** wurde erfolgreich geschlossen")
											.build()
							).queue();
						},
						() -> event.replyEmbeds(
								new EmbedBuilder()
										.setTimestamp(Instant.now())
										.setColor(Main.database.getColor(event.getGuild()))
										.setTitle(":exclamation: Error: Report not Found")
										.setDescription("Der Report #" + reportID + " konnte nicht gefunden werden!")
										.build()
						).setEphemeral(true).queue()
				);
	}
}
