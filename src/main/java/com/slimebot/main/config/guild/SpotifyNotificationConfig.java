package com.slimebot.main.config.guild;

import com.slimebot.main.config.guild.engine.ChannelField;
import com.slimebot.main.config.guild.engine.RoleField;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class SpotifyNotificationConfig {
	@RoleField(title = "Spotify Rolle", description = "Die Rolle, die bei neuen Spotify-Releases erwähnt wird")
	public Long notificationRole;

	@ChannelField(title = "Musik-Kanal", description = "In diesen Kanal werden neue Musik-Releases gesendet")
	public Long musicChannel;
	@ChannelField(title = "Podcast Kanal", description = "In diesen Kanal werden neue Podcast Folgen gesendet")
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
