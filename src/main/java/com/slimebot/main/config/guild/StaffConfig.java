package com.slimebot.main.config.guild;

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.LinkedHashMap;
import java.util.Optional;

public class StaffConfig {
	public LinkedHashMap<String, String> roles = new LinkedHashMap<>();

	public Long channel;
	public Long message;

	public Optional<GuildMessageChannel> getChannel() {
		return GuildConfig.getChannel(channel);
	}
}
