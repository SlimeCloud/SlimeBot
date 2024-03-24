package de.slimecloud.slimeball.features.alerts.youtube;

import de.slimecloud.slimeball.config.engine.Required;
import lombok.Getter;

@Getter
public class YoutubeConfig {

	@Required
	private String youtubeChannelId;

	@Required
	private int updateRate; // seconds

}
