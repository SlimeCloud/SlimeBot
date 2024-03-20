package de.slimecloud.slimeball.features.youtube;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import lombok.Getter;

import java.util.Map;

@Getter
public class GuildYoutubeConfig extends ConfigCategory {

	@ConfigField(name = "Kanal", command = "channel", description = "Der Text Kanal in den neue Videos gesendet werden", type = ConfigFieldType.MESSAGE_CHANNEL)
	private Long channel;

	@ConfigField(name = "Rolle", command = "role", description = "Die Rolle die bei neuen Videos gepingt wird", type = ConfigFieldType.ROLE)
	private Long role;

	@ConfigField(name = "Live Nachricht", command = "live-msg", description = "Die nachricht die bei livestreams gesendet wird", type = ConfigFieldType.STRING)
	private String liveMessage;

	@ConfigField(name = "Video Nachricht", command = "video-msg", description = "Die nachricht die bei neuen Videos gesendet wird", type = ConfigFieldType.STRING)
	private String videoMessage;

	@ConfigField(name = "Youtube Kanäle", command = "youtube-channels", description = "Youtube kanäle deren Livestreams und Videos gepostet werden", type = ConfigFieldType.STRING)
	private Map<String, String> youtubeChannels;

}
