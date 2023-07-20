package com.slimebot.main.config.guild;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class FdmdsConfig {
	public Long channel;
	public Long logChannel;
	public Long role;

	public Optional<GuildMessageChannel> getChannel() {
		return GuildConfig.getChannel(channel);
	}

	public Optional<GuildMessageChannel> getLogChannel() {
		return GuildConfig.getChannel(logChannel);
	}

	public Optional<Role> getRole() {
		return GuildConfig.getRole(role);
	}
}
