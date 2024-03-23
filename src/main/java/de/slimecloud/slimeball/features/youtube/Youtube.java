package de.slimecloud.slimeball.features.youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Youtube {

	private final String API_KEY;
	private final SlimeBot bot;
	private final List<GuildYoutube> guildInstances = new ArrayList<>();


	public void init() {
		bot.getJda().getGuilds().forEach(g -> bot.loadGuild(g).getYoutube().ifPresent(c -> guildInstances.add(new GuildYoutube(this, c))));
		bot.getExecutor().scheduleAtFixedRate(() -> guildInstances.forEach(GuildYoutube::schedule), 2, 2, TimeUnit.SECONDS);
	}

	public String getLastVideo(String id) {
		try {
			URL requestURL = new URI(String.format("https://www.googleapis.com/youtube/v3/search?key=%s&channelId=%s&part=snippet,id&order=date&maxResults=20", API_KEY, id)).toURL();
			HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
			urlConnection.setRequestMethod("GET");

			JsonObject response = JsonParser.parseReader(new InputStreamReader(urlConnection.getInputStream())).getAsJsonObject();
			JsonArray videos = response.getAsJsonArray("items");

			if(videos.size() <= 0) return "";
			return videos.get(0).getAsJsonObject().getAsJsonObject("id").get("videoId").getAsString();
		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
