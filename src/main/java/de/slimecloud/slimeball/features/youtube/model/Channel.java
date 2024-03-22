package de.slimecloud.slimeball.features.youtube.model;

public record Channel(String id, ChannelSnippet snippet) {

	public String getTitle() {
		return snippet.title();
	}
}
