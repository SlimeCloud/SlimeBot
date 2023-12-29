package de.slimecloud.slimeball.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SpotifyConfig {
	private ListenerConfig music;

	private ListenerConfig podcast;

	@NotNull
	public Optional<ListenerConfig> getMusicConfig() {
		return Optional.ofNullable(music);
	}

	@NotNull
	public Optional<ListenerConfig> getPodcastConfig() {
		return Optional.ofNullable(podcast);
	}

	public record ListenerConfig(@NotNull String message, @NotNull List<String> artistIds) {
	}
}
