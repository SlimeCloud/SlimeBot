package com.slimebot.utils;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Checks {

    public static Boolean hasTeamRole(Member member, Guild guild){
        Role staffRole = guild.getRoleById("1081650648124248124");  //ToDo get ID from a Config eg. Settings
                                                                    //MainID: 1077266943003865188
                                                                    //TestID: 1081650648124248124
        return !(member.getRoles().contains(staffRole));
    }

    public static Boolean isReportBlocked(Member member, TextChannel channel) {
        if (Main.blocklist.contains(member)){
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor)
                    .setTitle(":exclamation: Error: Blocked")
                    .setDescription("Du wurdest gesperrt, so dass du keine Reports mehr erstellen kannst");
            channel.sendMessageEmbeds(embedBuilder.build()).queue(); //ToDo Ephemeral
            return true;
        }else {
            return false;
        }
    }


}
