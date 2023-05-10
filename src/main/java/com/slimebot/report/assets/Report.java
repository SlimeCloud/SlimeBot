package com.slimebot.report.assets;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
        return Button.danger("close_report", "Close #" + reportID); //ToDo Java is wierd pls add 🔒 emoji
    }

    public static MessageEmbed getReportAsEmbed(Report report){

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
                .setColor(Main.embedColor)
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




