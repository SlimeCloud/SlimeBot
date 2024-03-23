package de.slimecloud.slimeball.features.youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.slimecloud.slimeball.features.youtube.model.SearchResult;
import de.slimecloud.slimeball.features.youtube.model.Video;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Youtube {

	private final String API_KEY;
	private final SlimeBot bot;

	private Video lastVideo;


	public void init() {
		int delay = bot.getConfig().getYoutube().get().getUpdateRate();
		lastVideo = getLastVideo();
		bot.getExecutor().scheduleAtFixedRate(this::schedule, delay, delay, TimeUnit.SECONDS);
	}

	private void schedule() {
		Video lastCheckedVideo = getLastVideo();
		if (lastCheckedVideo != null && !lastCheckedVideo.id().equals(lastVideo.id())) {
			new YoutubeVideoEvent(lastCheckedVideo).callEvent();
			this.lastVideo = lastCheckedVideo;
		}
	}

	//returns null if no videos found on the channel
	@Nullable
	public Video getLastVideo() {
		try {
			URL requestURL = new URI(String.format("https://www.googleapis.com/youtube/v3/search?key=%s&channelId=%s&part=snippet,id&order=date&maxResults=1", API_KEY, bot.getConfig().getYoutube().get().getYoutubeChannelId())).toURL();
			HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
			urlConnection.setRequestMethod("GET");

			JsonObject response = JsonParser.parseReader(new InputStreamReader(urlConnection.getInputStream())).getAsJsonObject();
			JsonArray videos = response.getAsJsonArray("items");

			if(videos.size() <= 0) return null;

			return Video.ofSearch(Main.json.fromJson(videos.get(0), SearchResult.class));
		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
