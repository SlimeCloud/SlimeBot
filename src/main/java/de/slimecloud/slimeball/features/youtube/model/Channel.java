package de.slimecloud.slimeball.features.youtube.model;

import lombok.Data;

@Data
public class Channel {

	private String id;

	private ChannelSnippet snippet;

	public String getTitle() {
		return getSnippet().getTitle();
	}
}
