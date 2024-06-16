package de.slimecloud.slimeball.features.alerts.youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.slimecloud.slimeball.config.YoutubeConfig;
import de.slimecloud.slimeball.features.alerts.youtube.model.SearchResult;
import de.slimecloud.slimeball.features.alerts.youtube.model.Video;
import de.slimecloud.slimeball.main.Main;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.MathUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Youtube {

	private final OkHttpClient client = new OkHttpClient().newBuilder().build();

	private final String[] keys;
	private final SlimeBot bot;
	private final YoutubeConfig config;

	private int currentKey;

	public Youtube(String[] keys, SlimeBot bot, YoutubeConfig config) {
		this.keys = keys;
		this.bot = bot;
		this.config = config;
		this.currentKey = MathUtil.randomInt(0, keys.length - 1);
	}

	public void startListener() {
		int delay = config.getUpdateRate();

		bot.getExecutor().scheduleAtFixedRate(() -> {
			try {
				check();
			} catch (Exception e) {
				logger.error("Failed to check for new video", e);
			}
		}, 0, delay, TimeUnit.SECONDS);
	}

	private void check() throws IOException {
		Set<Video> videos = getLastVideo(5);

		Collection<String> ids = bot.getIdMemory().getMemory("youtube");

		for (Video video : videos) {
			if (!ids.contains(video.id())) {
				new YoutubeVideoEvent(video).callEvent();
				ids.add(video.id());
			}
		}

		bot.getIdMemory().rememberIds("youtube", ids);
	}

	@NotNull
	private String getNextKey() {
		return keys[currentKey++ % keys.length];
	}

	//returns null if no videos found on the channel
	@NotNull
	public Set<Video> getLastVideo(int limit) throws IOException {
		Request request = new Request.Builder()
				.url(String.format("https://www.googleapis.com/youtube/v3/search?key=%s&channelId=%s&part=snippet,id&order=date&maxResults=" + limit, getNextKey(), config.getYoutubeChannelId()))
				.get()
				.build();

		@Cleanup
		Response response = client.newCall(request).execute();
		JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
		JsonArray videos = json.getAsJsonArray("items");

		Set<Video> result = new HashSet<>();

		for (JsonElement video : videos) {
			result.add(Video.ofSearch(Main.json.fromJson(video, SearchResult.class)));
		}

		return result;
	}
}
