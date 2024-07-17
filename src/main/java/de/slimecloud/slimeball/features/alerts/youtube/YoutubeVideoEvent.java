package de.slimecloud.slimeball.features.alerts.youtube;

import de.cyklon.jevent.Event;
import de.slimecloud.slimeball.features.alerts.youtube.model.Video;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class YoutubeVideoEvent extends Event {

	private final String youtubeChannelId;
	private final Video video;

	public boolean isLive() {
		return video.isLive();
	}
}
