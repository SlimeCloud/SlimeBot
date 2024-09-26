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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.hc.core5.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Youtube {

	private final OkHttpClient client = new OkHttpClient().newBuilder().build();

	@Getter
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
		Map<String, Integer> channels = config.getYoutubeChannels();

		channels.forEach((channel, delay) -> {
			bot.getExecutor().scheduleAtFixedRate(() -> {
				try {
					check(channel, currentKey);
				} catch (Exception e) {
					logger.error("Failed to check for new video for channel " + channel, e);
				}
			}, 0, delay, TimeUnit.SECONDS);
		});
	}

	public void check(String youtubeChannelId, int startKey) throws IOException {
		Set<Video> videos = getLastVideo(youtubeChannelId, 5, startKey);

		Collection<String> known = bot.getIdMemory().getMemory("youtube");
		List<String> newIds = new ArrayList<>();

		for (Video video : videos) {
			if (!known.contains(video.id())) {
				new YoutubeVideoEvent(this, youtubeChannelId, video).callEvent();
				newIds.add(video.id());
			}
		}

		bot.getIdMemory().rememberIds("youtube", newIds);
	}

	@NotNull
	private String getNextKey() {
		return keys[currentKey++ % keys.length];
	}

	@NotNull
	public Set<Video> getLastVideo(@NotNull String youtubeChannelId, int limit, int startKey) throws IOException {
		Request request = new Request.Builder()
				.url(String.format("https://www.googleapis.com/youtube/v3/search?key=%s&channelId=%s&part=snippet,id&order=date&maxResults=" + limit, getNextKey(), youtubeChannelId))
				.get()
				.build();

		@Cleanup
		Response response = client.newCall(request).execute();
		int code = response.code();

		JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();

		if (code != HttpStatus.SC_OK) {
			YoutubeApiErrorEvent event = new YoutubeApiErrorEvent(this, youtubeChannelId, startKey, response, json, new HashSet<>());
			event.callEvent();
			return event.getVideos();
		}

		JsonArray videos = json.getAsJsonArray("items");

		Set<Video> result = new HashSet<>();

		for (JsonElement video : videos) {
			Video videoResult = Video.ofSearch(Main.json.fromJson(video, SearchResult.class));
			if (videoResult != null) result.add(videoResult);
		}

		return result;
	}
}
