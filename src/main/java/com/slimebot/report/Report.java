package com.slimebot.report;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.IntFunction;

public class Report {
	public int id;
	public Type type;
	public User user;
	public User by;
	public LocalDateTime time;
	public Status status = Status.OPEN;
	public String msgContent;
	public String closeReason = "None";

	public Report(int id, Type type, User user, User by, String msgContent) {
		this.id = id;
		this.type = type;
		this.user = user;
		this.by = by;
		this.msgContent = msgContent;
		this.time = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static Button closeButton(int reportID) {
		return Button.danger("report:close", "Close #" + reportID).withEmoji(Emoji.fromUnicode("\uD83D\uDD12"));
	}

	public static void createReport(IReplyCallback event, IntFunction<Report> creator, User target) {
		YamlFile reportFile = Config.getConfig(event.getGuild().getId(), "reports");
		try {
			reportFile.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		int reportID = reportFile.getConfigurationSection("reports").size() + 1;
		Report report = creator.apply(reportID);

		save(event.getGuild().getId(), report);
		report.log(event.getGuild());

		event.replyEmbeds(
				new EmbedBuilder()
						.setTimestamp(Instant.now())
						.setColor(Main.embedColor(event.getGuild().getId()))
						.setTitle(":white_check_mark: Report Erfolgreich")
						.setDescription(target.getAsMention() + " wurde erfolgreich gemeldet")
						.build()
		).setEphemeral(true).queue();
	}


	public static Report get(String guildID, int reportID) {
		YamlFile reportFile = Config.getConfig(guildID, "reports");

		try {
			reportFile.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		if(!reportFile.contains("reports." + reportID + ".id")) {
			return null;
		}

		Report report = new Report(
				reportFile.getInt("reports." + reportID + ".id"),
				Type.valueOf(reportFile.getString("reports." + reportID + ".type")),
				Main.jdaInstance.getUserById(reportFile.getString("reports." + reportID + ".user")),
				Main.jdaInstance.getUserById(reportFile.getString("reports." + reportID + ".by")),
				reportFile.getString("reports." + reportID + ".msgContent")
		);

		report.time = LocalDateTime.parse(reportFile.getString("reports." + reportID + ".time"));
		report.status = Status.valueOf(reportFile.getString("reports." + reportID + ".status"));
		report.closeReason = reportFile.getString("reports." + reportID + ".closeReason");

		return report;
	}

	public static void save(String guildID, Report report) {
		YamlFile reportFile = Config.getConfig(guildID, "reports");

		if(!reportFile.exists()) {
			try {
				reportFile.createNewFile();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			reportFile.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		reportFile.set("reports." + report.id + ".id", report.id);
		reportFile.set("reports." + report.id + ".type", report.type.toString());
		reportFile.set("reports." + report.id + ".user", report.user.getId());
		reportFile.set("reports." + report.id + ".by", report.by.getId());
		reportFile.set("reports." + report.id + ".time", report.time.toString());
		reportFile.set("reports." + report.id + ".status", report.status.toString());
		reportFile.set("reports." + report.id + ".msgContent", report.msgContent);
		reportFile.set("reports." + report.id + ".closeReason", report.closeReason);

		try {
			reportFile.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void log(Guild guild) {
		YamlFile config = Config.getConfig(guild.getId(), "mainConfig");

		try {
			config.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		MessageChannel logChannel = Main.jdaInstance.getChannelById(MessageChannel.class, config.getString("punishmentChannelID"));

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTimestamp(Instant.now())
				.setColor(Main.embedColor(guild.getId()))
				.setTitle(":exclamation: Neuer Report!")
				.addField("Report von:", by.getAsMention(), true)
				.addField("Gemeldet:", user.getAsMention(), true);

		if(type == Type.MSG) {
			embedBuilder
					.setDescription("Es wurde eine Nachricht gemeldet!")
					.addField("Nachricht:", msgContent, false);
		}

		else {
			embedBuilder
					.setDescription("Es wurde eine Person gemeldet!")
					.addField("Begründung:", msgContent, false);
		}

		logChannel.sendMessageEmbeds(embedBuilder.build()).addActionRow(closeButton(id)).queue();
	}

	public MessageEmbed asEmbed(String guildID) {
		EmbedBuilder embed = new EmbedBuilder()
				.setColor(Main.embedColor(guildID))
				.setTimestamp(Instant.now())
				.setTitle(":exclamation:  Details zu Report #" + id)
				.addField("Report Typ:", type.str, true)
				.addField("Gemeldeter User:", user.getAsMention(), true)
				.addField("Gemeldet von:", by.getAsMention(), true)
				.addField("Gemeldet am:", time.format(Main.dtf) + "Uhr", true)
				.addField("Status:", status.str, true);

		if(type == Type.MSG) {
			embed.addField("Gemeldete Nachricht:", msgContent, false);
		}

		else if(type == Type.USER) {
			embed.addField("Meldegrund:", msgContent, true);
		}

		if(status == Status.CLOSED) {
			embed.addField("Verfahren:", closeReason, true);
		}

		return embed.build();
	}
}




