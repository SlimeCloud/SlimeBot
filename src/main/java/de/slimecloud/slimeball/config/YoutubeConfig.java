package de.slimecloud.slimeball.config;

import de.slimecloud.slimeball.config.engine.Required;
import lombok.Getter;

import java.util.Map;

@Getter
public class YoutubeConfig {

	/**
	 * <channel_id, update_rate>
	 * update rate in seconds
	 */
	@Required
	private Map<String, Integer> youtubeChannels;

}
