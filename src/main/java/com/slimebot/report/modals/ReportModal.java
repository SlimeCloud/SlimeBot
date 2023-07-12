package com.slimebot.report.modals;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class ReportModal extends ListenerAdapter {

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		if(!event.getModalId().equals("userReport")) return;

		User user = event.getJDA().getUserById(event.getValue("id").getAsString());
		String description = event.getValue("usrDescr").getAsString();

		Report report = Report.createReport(event.getGuild(), Type.USER, event.getUser(), user, description);

		event.replyEmbeds(
				new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(Main.database.getColor(event.getGuild()))
						.setTitle(":white_check_mark: Report Erfolgreich")
						.setDescription(user.getAsMention() + " wurde erfolgreich gemeldet")
						.build()
		).setEphemeral(true).queue();

		report.log();
	}
}
