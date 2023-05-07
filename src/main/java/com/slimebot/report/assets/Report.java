package com.slimebot.report.assets;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.sql.Time;
import java.time.LocalDateTime;

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
        report.time = LocalDateTime.now();
        report.msgContent = msgContent;

        return report;
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




