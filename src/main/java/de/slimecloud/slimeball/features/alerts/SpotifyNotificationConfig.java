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

	@ConfigField(name = "Musik-Rolle", command = "music-role", description = "Rolle, die bei neuen Musik-Releases erwähnt wird", type = ConfigFieldType.ROLE)
	private Long musicRole;

	@ConfigField(name = "Podcast-Rolle", command = "podcast-role", description = "Rolle, die bei neuen Podcast Folgen erwähnt wird", type = ConfigFieldType.ROLE)
	private Long podcastRole;

	@NotNull
	public Optional<GuildMessageChannel> getMusicChannel() {
		return Optional.ofNullable(musicChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<GuildMessageChannel> getPodcastChannel() {
		return Optional.ofNullable(podcastChannel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public Optional<Role> getMusicRole() {
		return Optional.ofNullable(musicRole).map(bot.getJda()::getRoleById);
	}

	@NotNull
	public Optional<Role> getPodcastRole() {
		return Optional.ofNullable(podcastRole).map(bot.getJda()::getRoleById);
	}
}
