package de.slimecloud.slimeball.features.alerts.youtube;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.Info;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class GuildYoutubeConfig extends ConfigCategory {
	@ConfigField(name = "Kanal", command = "channel", description = "Der Text Kanal in den neue Videos gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	@Info(keyType = ConfigFieldType.STRING)
	private Map<String, Long> channel = new HashMap<>();

	@ConfigField(name = "Rolle", command = "role", description = "Die Rolle, die bei neuen Videos gepingt wird", type = ConfigFieldType.ROLE)
	@Info(keyType = ConfigFieldType.STRING)
	private Map<String, Long> role = new HashMap<>();

	@ConfigField(name = "Live Nachricht", command = "live-msg", description = "Die Nachricht, die bei livestreams gesendet wird; Placeholder: %role% %uploader% %url% %title%", type = ConfigFieldType.STRING)
	@Info(keyType = ConfigFieldType.STRING)
	private Map<String, String> liveMessage = new HashMap<>();

	@ConfigField(name = "Video Nachricht", command = "video-msg", description = "Die Nachricht, die bei neuen Videos gesendet wird; Placeholder: %role% %uploader% %url% %title%", type = ConfigFieldType.STRING)
	@Info(keyType = ConfigFieldType.STRING)
	private Map<String, String> videoMessage = new HashMap<>();

	@NotNull
	public Optional<Long> getChannelId(@NotNull String youtubeChannelId) {
		return Optional.ofNullable(channel.get(youtubeChannelId));
	}

	@NotNull
	public Optional<MessageChannel> getChannel(@NotNull String youtubeChannelId) {
		return getChannelId(youtubeChannelId).flatMap(id -> Optional.ofNullable(bot.getJda().getChannelById(MessageChannel.class, id)));
	}

	@NotNull
	public Optional<Role> getRole(@NotNull String youtubeChannelId) {
		return Optional.ofNullable(bot.getJda().getRoleById(role.get(youtubeChannelId)));
	}
}
