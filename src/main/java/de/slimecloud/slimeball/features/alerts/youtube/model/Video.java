package de.slimecloud.slimeball.features.alerts.youtube.model;

public record Video(String id, VideoSnippet snippet) {

	public String getUrl() {
		return "https://www.youtube.com/watch?v=" + id;
	}

	public boolean isLive() {
		return snippet.liveBroadcastContent().equals("live");
	}

	public Channel getChannel() {
		return new Channel(snippet.channelId(), new ChannelSnippet(snippet.channelTitle()));
	}

	public static Video ofSearch(SearchResult sr) {
		SearchResultSnippet snippet = sr.snippet();
		return new Video(sr.id().videoId(), new VideoSnippet(snippet.description(), snippet.title(), snippet.channelId(), snippet.channelTitle(), snippet.liveBroadcastContent()));
	}

}
