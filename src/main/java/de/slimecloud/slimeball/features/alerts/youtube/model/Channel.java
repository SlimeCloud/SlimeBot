package de.slimecloud.slimeball.features.alerts.youtube.model;

public record Channel(String id, ChannelSnippet snippet) {

	public String getTitle() {
		return snippet.title();
	}
}
