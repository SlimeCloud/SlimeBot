package de.slimecloud.slimeball.features.youtube;

import de.cyklon.jevent.Event;
import de.slimecloud.slimeball.features.youtube.model.Video;
import lombok.Getter;

@Getter
public class YoutubeVideoEvent extends Event {

	private final Video video;

	public YoutubeVideoEvent(Video video) {
		this.video = video;
	}

	public boolean isLive() {
		return video.isLive();
	}
}
