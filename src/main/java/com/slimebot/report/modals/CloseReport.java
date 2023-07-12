package com.slimebot.report.modals;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

public class CloseReport extends ListenerAdapter {
	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if(!event.getModalId().equals("report:close")) return;

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
