package com.slimebot.events;

import com.slimebot.main.config.guild.AssignRoleConfig;
import com.slimebot.main.config.guild.GuildConfig;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class MemberJoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        GuildConfig.getConfig(event.getGuild().getIdLong()).getAssignRole().flatMap(AssignRoleConfig::getRoles).ifPresent(
                roles -> roles.forEach(r -> event.getGuild().addRoleToMember(event.getMember(), r).queue())
        );

    }
}
