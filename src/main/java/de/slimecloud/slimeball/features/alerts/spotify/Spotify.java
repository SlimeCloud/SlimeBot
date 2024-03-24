package de.slimecloud.slimeball.features.alerts.spotify;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;

import java.io.IOException;

@Slf4j
public class Spotify {
	private final SpotifyApi api;
	private long expiry = 0;

	public Spotify(@NotNull String id, @NotNull String secret) {
		api = new SpotifyApi.Builder()
				.setClientId(id)
				.setClientSecret(secret)
				.build();

		fetchToken();
	}

	@NotNull
	public SpotifyApi getApi() {
		if (System.currentTimeMillis() > expiry - 10000) fetchToken(); //Refresh token 10 seconds before expiry
		return api;
	}

	private void fetchToken() {
		try {
			logger.info("Refreshing spotify token");

			ClientCredentials credentials = api.clientCredentials().build().execute();
			api.setAccessToken(credentials.getAccessToken());
			expiry = System.currentTimeMillis() + credentials.getExpiresIn() * 1000;
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			logger.error("Fetching spotify token failed", e);
		}
	}
}
