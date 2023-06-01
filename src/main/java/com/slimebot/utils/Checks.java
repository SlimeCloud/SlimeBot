package com.slimebot.utils;

import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Checks {

    public static Boolean hasTeamRole(Member member, Guild guild){
        Role staffRole = guild.getRoleById(Config.getProperty(Config.botPath + guild.getId() + "/config.yml", "staffRoleId"));
        return !(member.getRoles().contains(staffRole));
    }


}
