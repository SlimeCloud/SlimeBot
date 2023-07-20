package com.slimebot.main.config.guild;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class SpotifyNotificationConfig {
	public Long notificationRole;
	public Long musicChannel;
	public Long podcastChannel;

	public Optional<GuildMessageChannel> getMusicChannel() {
		return GuildConfig.getChannel(musicChannel);
	}

	public Optional<GuildMessageChannel> getPodcastChannel() {
		return GuildConfig.getChannel(podcastChannel);
	}

	public Optional<Role> getRole() {
		return GuildConfig.getRole(notificationRole);
	}
}
