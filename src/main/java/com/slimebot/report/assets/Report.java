package com.slimebot.report.assets;

import com.slimebot.main.Main;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Report {
	public int id;
	public Type type;
	public Member user;
	public Member by;
	public LocalDateTime time;
	public Status status = Status.OPEN;
	public String msgContent;
	public String closeReason = "Nonde";

	public Report(int id, Type type, Member user, Member by, String msgContent) {
		this.id = id;
		this.type = type;
		this.user = user;
		this.by = by;
		this.msgContent = msgContent;
		this.time = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static Button closeButton(int reportID) {
		return Button.danger("close_report", "Close #" + reportID).withEmoji(Emoji.fromUnicode("\uD83D\uDD12"));
	}

	public static void log(Integer reportID, String guildID) {
		YamlFile config = Config.getConfig(guildID, "mainConfig");

		Report newReport = get(guildID, reportID);

		try {
			config.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		TextChannel logChannel = Main.jdaInstance.getTextChannelById(config.getString("punishmentChannelID"));

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
				.setColor(Main.embedColor(guildID))
				.setTitle(":exclamation: Neuer Report!")
				.addField("Report von:", newReport.by.getAsMention(), true)
				.addField("Gemeldet:", newReport.user.getAsMention(), true);

		if(newReport.type == Type.MSG) {
			embedBuilder
					.setDescription("Es wurde eine Nachricht gemeldet!")
					.addField("Nachricht:", newReport.msgContent, false);
		}

		else {
			embedBuilder
					.setDescription("Es wurde eine Person gemeldet!")
					.addField("Begründung:", newReport.msgContent, false);
		}

		logChannel.sendMessageEmbeds(embedBuilder.build()).addActionRow(closeButton(reportID)).queue();
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

		System.out.println(reportFile.getString("reports." + report.id + ".msgContent"));
	}

	public static Report get(String guildID, Integer reportID) {
		YamlFile reportFile = Config.getConfig(guildID, "reports");

		try {
			reportFile.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		Guild guild = Main.jdaInstance.getGuildById(guildID);

		Report report = new Report(
				reportFile.getInt("reports." + reportID + ".id"),
				Type.valueOf(reportFile.getString("reports." + reportID + ".type")),
				guild.getMemberById(reportFile.getString("reports." + reportID + ".user")),
				guild.getMemberById(reportFile.getString("reports." + reportID + ".by")),
				reportFile.getString("reports." + reportID + ".msgContent")
		);

		report.time = LocalDateTime.parse(reportFile.getString("reports." + reportID + ".time"));
		report.status = Status.valueOf(reportFile.getString("reports." + reportID + ".status"));
		report.closeReason = reportFile.getString("reports." + reportID + ".closeReason");

		return report;
	}

	public MessageEmbed asEmbed(String guildID) {
		EmbedBuilder embed = new EmbedBuilder()
				.setColor(Main.embedColor(guildID))
				.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
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




