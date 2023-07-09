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
        report.setId(id);
        report.setType(type);
        report.setUser(user);
        report.setBy(by);
        report.setTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        report.setStatus(Status.OPEN);
        report.setMsgContent(msgContent);
        report.setCloseReason("None");

        return report;
    }

    public static Button closeBtn(String reportID){
        return Button.danger("close_report", "Close #" + reportID).withEmoji(Emoji.fromUnicode("\uD83D\uDD12"));
    }

    public static void log(Integer reportID, String guildID){
        YamlFile config = Config.getConfig(guildID, "mainConfig");

        Report newReport = get(guildID, reportID);

        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TextChannel logChannel = Main.jdaInstance.getTextChannelById(config.getString("punishmentChannelID"));

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setColor(Main.embedColor(guildID))
                .setTitle(":exclamation: Neuer Report!")
                .addField("Report von:", newReport.getBy().getAsMention(), true)
                .addField("Gemeldet:", newReport.getUser().getAsMention(), true);

        if (newReport.getType() == Type.MSG){
            embedBuilder.setDescription("Es wurde eine Nachricht gemeldet!")
                    .addField("Nachricht:", newReport.getMsgContent(), false);
        } else {
            embedBuilder.setDescription("Es wurde eine Person gemeldet!")
                    .addField("Begründung:", newReport.getMsgContent(), false);
        }


        logChannel.sendMessageEmbeds(embedBuilder.build()).addActionRow(closeBtn(reportID.toString())).queue();
    }

    public static void save(String guildID, Report report){
        YamlFile reportFile = Config.getConfig(guildID, "reports");

        if (!reportFile.exists()){
            try {
                reportFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            reportFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String rID = String.valueOf(report.getId());

        reportFile.set("reports."+ rID +".id", report.getId());
        reportFile.set("reports."+ rID +".type", report.getType().toString());
        reportFile.set("reports."+ rID +".user", report.getUser().getId());
        reportFile.set("reports."+ rID +".by", report.getBy().getId());
        reportFile.set("reports."+ rID +".time", report.getTime().toString());
        reportFile.set("reports."+ rID +".status", report.getStatus().toString());
        reportFile.set("reports."+ rID +".msgContent", String.valueOf(report.getMsgContent()));
        reportFile.set("reports."+ rID +".closeReason", report.getCloseReason().toString());

        try {
            reportFile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static Report get(String guildID, Integer reportID){
        YamlFile reportFile = Config.getConfig(guildID, "reports");
        Report report = new Report();
        Guild guild = Main.getJDAInstance().getGuildById(guildID);

        try {
            reportFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        report.setId(reportFile.getInt("reports."+ reportID+".id"));
        String rID = reportID.toString();

        if (Objects.equals(reportFile.getString("reports." + rID + ".type"), "MSG")){
            report.setType(Type.MSG);
        } else {
            report.setType(Type.USER);
        }

        report.setUser(guild.getMemberById(reportFile.getString("reports."+ rID+".user")));
        report.setBy(guild.getMemberById(reportFile.getString("reports."+ rID+".by")));
        report.setTime(LocalDateTime.parse(reportFile.getString("reports."+ rID+".time")));

        if (Objects.equals(reportFile.getString("reports." + rID + ".status"), "OPEN")){
            report.setStatus(Status.OPEN);
        } else {
            report.setStatus(Status.CLOSED);
        }


        String msgC = reportFile.getString("reports."+ rID +".msgContent");
        String clR = reportFile.getString("reports."+ rID +".closeReason");
        report.setCloseReason(clR);
        report.setMsgContent(msgC);

        return report;
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

        return embed.build();


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




