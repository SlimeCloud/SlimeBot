package com.slimebot.events;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Timeout extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
        super.onGuildMemberUpdateTimeOut(event);
        if (!event.getMember().isTimedOut()){return;}

        for (AuditLogEntry entry: event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE)){
            if (entry.getTargetId().equals(event.getMember().getId())){

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Du wurdest getimeouted")
                        .setColor(Main.embedColor(event.getGuild().getId()))
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .setDescription("Du wurdest auf dem SlimeCloud Discord getimeouted")
                        .addField("Grund:", entry.getReason(), true);

                event.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(embedBuilder.build())).queue();


                EmbedBuilder embedBuilderLog = new EmbedBuilder()
                        .setTitle("\"" + event.getMember().getEffectiveName() + "\"" + " wurde getimeouted")
                        .setColor(Main.embedColor(event.getGuild().getId()))
                        .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                        .addField("Grund:", entry.getReason(), true)
                        .addField("Wer: ", event.getMember().getAsMention(), true);

                event.getGuild().getTextChannelById("1080912327693574275").sendMessageEmbeds(embedBuilderLog.build()).queue();
                break;
            }
        }
    }
}
