package de.slimecloud.slimeball.features.youtube.model;

import lombok.Data;

@Data
public class Video {

	private String id;

	private VideoSnippet snippet;

	public boolean isLive() {
		return snippet.getLiveBroadcastContent().equals("live");
	}

}
