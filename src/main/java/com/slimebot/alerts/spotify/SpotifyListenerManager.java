package com.slimebot.alerts.spotify;

import org.apache.hc.core5.http.ParseException;
import org.simpleyaml.configuration.file.YamlFile;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class SpotifyListenerManager {
	private final YamlFile config = new YamlFile("Slimebot/spotify/config.yml");
	private final SpotifyApi api;

	public SpotifyListenerManager() {
		if(!config.exists()) {
			try {
				config.createNewFile();
				createConfig();
			} catch(IOException e) {
				SpotifyListener.logger.error("Spotify config konnte nicht erstellt werden");
			}
		}

		try {
			config.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		api = new SpotifyApi.Builder()
				.setClientId(config.getString("clientId"))
				.setClientSecret(config.getString("clientSecret"))
				.build();
		try {
			api.setAccessToken(api.clientCredentials().build().execute().getAccessToken());
		} catch(IOException | SpotifyWebApiException | ParseException e) {
			SpotifyListener.logger.error("Spotify login fehlgeshlagen", e);
		}
	}

	public void register() {
		String message = config.getString("message");

		for(Map.Entry<String, Object> entry : config.getConfigurationSection("artists").getMapValues(false).entrySet()) {
			String artistID = entry.getKey();
			new SpotifyListener(artistID, config, message, api);
		}

		for(Map.Entry<String, Object> entry : config.getConfigurationSection("show").getMapValues(false).entrySet()) {
			String showID = entry.getKey();
			new PodcastListener(showID, config, api);
		}
	}

	private void createConfig() {
		config.set("clientId", "");
		config.set("clientSecret", "");
		config.set("message", """
				<@&roleId>
				            
				## 🎶 ALARM ALARM 🎶
				Neuer Musik Release. 🥳
				                            
				Gönn dir umbedingt das neue Album: **{0}**
				            
				{1}
				""");
		config.setComment("message", "Format: {0} = Albumname, {1} = Link zum Album");
		config.set("artists.0ZzsW7JiW4Ok3H7nFl4yV1.channelId", 123456);
		config.set("artists.0ZzsW7JiW4Ok3H7nFl4yV1.publishedAlbums", new ArrayList<String>());
		config.setComment("artists", "Format: <artist id>: <channel id>");
		config.set("show.0HNYFHg2WNq1P56qK6defn.message", """
				<@&roleId>
				            
				## 🎙️ ALARM ALARM 🎙️
				Neue Podcast-Folge! Rein da!
				                
				                            
				In der heutigen Folge: **{0}**
				            
				{1}
				""");
		config.setComment("show.0HNYFHg2WNq1P56qK6defn.message", "Format: {0} = Podcasttitel, {1} = Link zum Podcast");
		config.set("show.0HNYFHg2WNq1P56qK6defn.channelId", 123456);
		config.set("show.0HNYFHg2WNq1P56qK6defn.publishedEpisodes", new ArrayList<String>());

		try {
			config.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
