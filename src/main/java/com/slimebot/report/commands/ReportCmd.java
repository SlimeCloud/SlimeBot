package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Type;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ReportCmd extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if(!event.getName().equals("report")) return;

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

		System.out.println(reportFile.getConfigurationSection("reports").getName());
		int reportID = reportFile.getConfigurationSection("reports").size() + 1;

		OptionMapping user = event.getOption("user");
		OptionMapping description = event.getOption("beschreibung");

		Report.save(event.getGuild().getId(), new Report(reportID, Type.USER, user.getAsMember(), event.getMember(), description.getAsString()));

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
				.setColor(Main.embedColor(event.getGuild().getId()))
				.setTitle(":white_check_mark: Report Erfolgreich")
				.setDescription(user.getAsMentionable().getAsMention() + " wurde erfolgreich gemeldet");

		event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
		Report.log(reportID, event.getGuild().getId());
	}
}
