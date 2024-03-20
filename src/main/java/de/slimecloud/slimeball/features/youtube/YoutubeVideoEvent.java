package de.slimecloud.slimeball.features.youtube;

import de.cyklon.jevent.Event;
import lombok.Getter;

@Getter
public class YoutubeVideoEvent extends Event {

	private final String videoID;
	private final String videoUrl;

	public YoutubeVideoEvent(String videoID) {
		this.videoID = videoID;
		this.videoUrl = "https://www.youtube.com/watch?v=" + videoID;
	}
}
