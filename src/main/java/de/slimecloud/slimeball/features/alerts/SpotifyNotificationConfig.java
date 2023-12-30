package de.slimecloud.slimeball.features.alerts;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Setter
public class SpotifyNotificationConfig extends ConfigCategory {
	@ConfigField(name = "Musik-Kanal", command = "music", description = "Kanal, in dem Benachrichtigungen über Musik-Releases gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long musicChannel;

	@ConfigField(name = "Podcast-Kanal", command = "podcast", description = "Kanal, in dem Benachrichtigungen über Podcast Folgen gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long podcastChannel;

	@ConfigField(name = "Benachrichtigungs-Rolle", command = "role", description = "Rolle, die bei neuen Spotify-Releases erwähnt wird", type = ConfigFieldType.ROLE)
	private Long notificationRole;

	@NotNull
	public Optional<GuildMessageChannel> getMusicChannel() {
		return Optional.ofNullable(musicChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<GuildMessageChannel> getPodcastChannel() {
		return Optional.ofNullable(podcastChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<Role> getRole() {
		return Optional.ofNullable(notificationRole).map(bot.getJda()::getRoleById);
	}
}
