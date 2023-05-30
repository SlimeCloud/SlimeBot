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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class Report {
    public Integer id;
    public Type type;
    public Member user;
    public Member by;
    public LocalDateTime time;
    public Status status;
    public String msgContent;
    public String closeReason;

    public static Report newReport(Integer id, Type type, Member user, Member by, String msgContent){
        Report report = new Report();
        report.id = id;
        report.type = type;
        report.user = user;
        report.by = by;
        report.closeReason = "None";
        report.status = Status.OPEN;
        report.time = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        report.msgContent = msgContent;

        return report;
    }

    public static Button closeBtn(String reportID){
        return Button.danger("close_report", "Close #" + reportID).withEmoji(Emoji.fromUnicode("\uD83D\uDD12"));
    }

    public static void log(Integer reportID, String guildID){

        Report newReport = null;
        for (Report report: Main.reports) {
            if (!(Objects.equals(report.id, reportID))) {
                continue;
            }
            newReport = report;
        }

        TextChannel logChannel = Main.jdaInstance.getTextChannelById(Config.getProperty("config.yml", "punishmentChannelID"));

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setColor(Main.embedColor(guildID))
                .setTitle(":exclamation: Neuer Report!")
                .addField("Report von:", newReport.by.getAsMention(), true)
                .addField("Gemeldet:", newReport.user.getAsMention(), true);

        if (newReport.type == Type.MSG){
            embedBuilder.setDescription("Es wurde eine Nachricht gemeldet!")
                    .addField("Nachricht:", newReport.msgContent, false);
        } else {
            embedBuilder.setDescription("Es wurde eine Person gemeldet!")
                    .addField("Begründung:", newReport.msgContent, false);
        }


        logChannel.sendMessageEmbeds(embedBuilder.build()).addActionRow(closeBtn(reportID.toString())).queue();
    }

    public static MessageEmbed getReportAsEmbed(Report report, String guildID){

        String TypeStr = "";
        switch (report.getType()) {
            case MSG -> TypeStr = "Nachricht";
            case USER -> TypeStr = "User";
        }

        String StatusStr = "";
        switch (report.getStatus()) {
            case CLOSED -> StatusStr = "Geschlossen";
            case OPEN -> StatusStr = "Offen";
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Main.embedColor(guildID))
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setTitle(":exclamation:  Details zu Report #" + report.getId().toString())
                .addField("Report Typ:", TypeStr, true)
                .addField("Gemeldeter User:", report.getUser().getAsMention(), true)
                .addField("Gemeldet von:", report.getBy().getAsMention(), true)
                .addField("Gemeldet am:", report.getTime().format(Main.dtf) + "Uhr", true)
                .addField("Status:", StatusStr, true);

        if (report.getType() == Type.MSG){
            embed.addField("Gemeldete Nachricht:", report.getMsgContent(), false);
        } else if (report.getType() == Type.USER) {
            embed.addField("Meldegrund:", report.getMsgContent(), true);
        }

        if (report.getStatus() == Status.CLOSED) {
            embed.addField("Verfahren:", report.getCloseReason(), true);
        }

        MessageEmbed eb = embed.build();



        return eb;


    }


    public Integer getId() {
        return id;
    }
    public Type getType() {
        return type;
    }
    public Member getUser() {return user;}
    public Member getBy() {
        return by;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Status getStatus() {
        return status;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setUser(Member user) {
        this.user = user;
    }

    public void setBy(Member by) {
        this.by = by;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }
}




