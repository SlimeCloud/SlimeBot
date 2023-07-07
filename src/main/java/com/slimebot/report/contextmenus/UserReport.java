package com.slimebot.report.contextmenus;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class UserReport extends ListenerAdapter {
	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if(!event.getName().equals("Report User")) return;

		if(Main.blocklist(event.getGuild().getId()).contains(event.getMember().getId())) {
			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
					.setColor(Main.embedColor(event.getGuild().getId()))
					.setTitle(":exclamation: Error: Blocked")
					.setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst");
			event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
			return;
		}

		YamlFile reportFile = Config.getConfig(event.getGuild().getId(), "reports");
		try {
			reportFile.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		int reportID = reportFile.getConfigurationSection("reports").size() + 1;

		Report.save(event.getGuild().getId(), new Report(reportID, Type.USER, event.getTargetMember(), event.getMember(), "None"));

		TextInput userReportDescription = TextInput.create("usrDescr", "Warum möchtest du diese Person Reporten?", TextInputStyle.SHORT)
				.setMinLength(15)
				.setMaxLength(500)
				.setRequired(true)
				.setPlaceholder("Hier deine Begründung")
				.build();

		TextInput saveId = TextInput.create("id", "Bitte die Report ID nicht ändern!", TextInputStyle.SHORT)
				.setRequired(true)
				.setValue(String.valueOf(reportID))
				.setPlaceholder("Diese feld wird automatisch ausgefüllt!")
				.build();

		Modal userReport = Modal.create("userReport", "User Reporten")
				.addActionRow(userReportDescription)
				.addActionRow(saveId)
				.build();

		event.replyModal(userReport).queue();
	}
}
