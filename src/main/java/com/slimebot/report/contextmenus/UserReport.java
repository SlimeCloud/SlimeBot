package com.slimebot.report.contextmenus;

import com.slimebot.main.Main;
import com.slimebot.report.commands.Blockreport;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.Instant;

public class UserReport extends ListenerAdapter {
	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if(!event.getFullCommandName().equals("Report User")) return;

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

		TextInput userReportDescription = TextInput.create("usrDescr", "Warum möchtest du diese Person Reporten?", TextInputStyle.SHORT)
				.setMinLength(15)
				.setMaxLength(500)
				.setRequired(true)
				.setPlaceholder("Hier deine Begründung")
				.build();

		TextInput saveId = TextInput.create("id", "Nutzer ID", TextInputStyle.SHORT)
				.setRequired(true)
				.setValue(event.getTarget().getId())
				.setPlaceholder("Diese feld wird automatisch ausgefüllt!")
				.build();

		Modal userReport = Modal.create("userReport", "User Reporten")
				.addActionRow(userReportDescription)
				.addActionRow(saveId)
				.build();

		event.replyModal(userReport).queue();
	}
}
