package de.slimecloud.slimeball.features.alerts.youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.slimecloud.slimeball.features.alerts.youtube.model.SearchResult;
import de.slimecloud.slimeball.features.alerts.youtube.model.Video;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Youtube {

	private final OkHttpClient client = new OkHttpClient().newBuilder().build();

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
			Request request = new Request.Builder()
					.url(String.format("https://www.googleapis.com/youtube/v3/search?key=%s&channelId=%s&part=snippet,id&order=date&maxResults=1", API_KEY, bot.getConfig().getYoutube().get().getYoutubeChannelId()))
					.get()
					.build();

			try (Response response = client.newCall(request).execute()) {
				JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
				JsonArray videos = json.getAsJsonArray("items");

				if(videos.size() <= 0) return null;

				return Video.ofSearch(Main.json.fromJson(videos.get(0), SearchResult.class));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
