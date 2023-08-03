package com.slimebot.events;

import com.slimebot.main.config.guild.GuildConfig;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class MemberJoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        GuildConfig.getConfig(event.getGuild().getIdLong()).getAssignRole().ifPresent(
                assignRoleConfig -> assignRoleConfig.getRole().ifPresentOrElse(
                        role -> event.getGuild().addRoleToMember(event.getMember(), role).queue(),
                        () -> logger.warn("no Role found")
                )
        );

    }
}
