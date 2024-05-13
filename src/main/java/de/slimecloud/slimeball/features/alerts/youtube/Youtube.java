package de.slimecloud.slimeball.features.alerts.youtube;

import com.google.gson.JsonArray;
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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Youtube {

	private final OkHttpClient client = new OkHttpClient().newBuilder().build();

	private final String[] keys;
	private final SlimeBot bot;
	private final YoutubeConfig config;

	private int currentKey;
	private Video lastVideo;

	public Youtube(String[] keys, SlimeBot bot, YoutubeConfig config) {
		this.keys = keys;
		this.bot = bot;
		this.config = config;
		this.currentKey = MathUtil.randomInt(0, keys.length - 1);
		logger.info("Initialize Youtube with " + keys.length + " keys and start index of " + currentKey);
	}

	public void startListener() {
		int delay = config.getUpdateRate();
		logger.info("start listener with delay of " + delay + "s");

		bot.getExecutor().scheduleAtFixedRate(() -> {
			try {
				check();
			} catch (Exception e) {
				logger.error("Failed to check for new video", e);
			}
		}, 0, delay, TimeUnit.SECONDS);
	}

	private void check() throws IOException {
		Video lastCheckedVideo = getLastVideo();
		logger.info("checking for new video");
		logger.info("previousCheckedVideo: " + lastVideo);
		logger.info("lastCheckedVideo: " + lastCheckedVideo);

		if (lastCheckedVideo != null && !lastCheckedVideo.equals(lastVideo)) {
			logger.info("previous and last checked are not equal");
			if (lastVideo != null) {
				logger.info("previous is not null -> new video uploaded");
				new YoutubeVideoEvent(lastCheckedVideo).callEvent();
			} else logger.info("previous is null -> no video checked before -> lastVideo = lastCheckedVideo");
			this.lastVideo = lastCheckedVideo;
		}
	}

	@NotNull
	private String getNextKey() {
		logger.info("getNextKey -> current index is " + currentKey);
		return keys[currentKey++ % keys.length];
	}

	//returns null if no videos found on the channel
	@Nullable
	public Video getLastVideo() throws IOException {
		logger.info("fetch last video");
		Request request = new Request.Builder()
				.url(String.format("https://www.googleapis.com/youtube/v3/search?key=%s&channelId=%s&part=snippet,id&order=date&maxResults=1", getNextKey(), config.getYoutubeChannelId()))
				.get()
				.build();;

		@Cleanup
		Response response = client.newCall(request).execute();
		logger.info("api response: " + response);
		JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
		logger.info("api json: " + json);
		JsonArray videos = json.getAsJsonArray("items");
		logger.info("api json array: " + videos);;

		if (videos.size() <= 0) return null;

		SearchResult sr = Main.json.fromJson(videos.get(0), SearchResult.class);
		logger.info("SearchResult of json: " + sr);

		Video video = Video.ofSearch(sr);
		logger.info("Fetch Video result: " + video);

		return video;
	}
}
