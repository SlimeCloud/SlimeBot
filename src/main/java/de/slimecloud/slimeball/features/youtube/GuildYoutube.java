package de.slimecloud.slimeball.features.youtube;

import java.util.HashMap;
import java.util.Map;

public class GuildYoutube {

	private final Youtube youtube;
	private final Map<String, String> lastVideo = new HashMap<>();

	public GuildYoutube(Youtube youtube, GuildYoutubeConfig config) {
		this.youtube = youtube;
		config.getYoutubeChannels().keySet().forEach(id -> lastVideo.put(id, youtube.getLastVideo(id)));
	}

	public void schedule() {
		lastVideo.keySet().forEach(id -> {
			if (hasNewVideo(id)) {
				String videoId = youtube.getLastVideo(id);
				lastVideo.put(id, videoId);
				new YoutubeVideoEvent(videoId).callEvent();
			}
		});
	}

	public boolean hasNewVideo(String id) {
		return !youtube.getLastVideo(id).equals(lastVideo.get(id));
	}
}
