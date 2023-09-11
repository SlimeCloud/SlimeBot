package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class SpotifyNotificationConfig {
	@ConfigField(type = ConfigFieldType.ROLE, command = "notification_role", title = "Spotify Rolle", description = "Die Rolle, die bei neuen Spotify-Releases erwähnt wird")
	public Long notificationRole;

	@ConfigField(type = ConfigFieldType.CHANNEL, command = "music_channel", title = "Musik-Kanal", description = "In diesen Kanal werden neue Musik-Releases gesendet")
	public Long musicChannel;
	@ConfigField(type = ConfigFieldType.CHANNEL, command = "podcast_channel", title = "Podcast Kanal", description = "In diesen Kanal werden neue Podcast Folgen gesendet")
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
