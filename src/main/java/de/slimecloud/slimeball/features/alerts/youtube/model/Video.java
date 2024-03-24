package de.slimecloud.slimeball.features.alerts.youtube.model;

import org.jetbrains.annotations.NotNull;

public record Video(String id, VideoSnippet snippet) {

	@NotNull
	public String getUrl() {
		return "https://www.youtube.com/watch?v=" + id;
	}

	public boolean isLive() {
		return snippet.liveBroadcastContent().equals("live");
	}

	@NotNull
	public Channel getChannel() {
		return new Channel(snippet.channelId(), new ChannelSnippet(snippet.channelTitle()));
	}

	@NotNull
	public static Video ofSearch(SearchResult sr) {
		SearchResultSnippet snippet = sr.snippet();
		return new Video(sr.id().videoId(), new VideoSnippet(snippet.description(), snippet.title(), snippet.channelId(), snippet.channelTitle(), snippet.liveBroadcastContent()));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Video v && id.equals(v.id);
	}
}
