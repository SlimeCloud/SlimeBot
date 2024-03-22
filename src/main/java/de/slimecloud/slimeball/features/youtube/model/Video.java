package de.slimecloud.slimeball.features.youtube.model;

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

}
