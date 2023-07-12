package com.slimebot.alerts.spotify;

import com.slimebot.main.Main;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Map;

public class SpotifyListenerManager {
	private final SpotifyApi api;

	public SpotifyListenerManager() {
		api = new SpotifyApi.Builder()
				.setClientId(Main.config.spotify.clientId)
				.setClientSecret(Main.config.spotify.clientSecret)
				.build();
		try {
			api.setAccessToken(api.clientCredentials().build().execute().getAccessToken());
		} catch(IOException | SpotifyWebApiException | ParseException e) {
			SpotifyListener.logger.error("Spotify login fehlgeshlagen", e);
		}
	}

	public void register() {
		Main.config.spotify.music.artistIds.forEach(id -> new SpotifyListener(api, id));
		Main.config.spotify.podcast.artistIds.forEach(id -> new PodcastListener(api, id));
	}
}
