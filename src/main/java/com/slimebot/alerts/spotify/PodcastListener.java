package com.slimebot.alerts.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.hc.core5.http.ParseException;
import org.simpleyaml.configuration.file.YamlFile;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.EpisodeSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PodcastListener implements Runnable {
	private final YamlFile config;
	private final SpotifyApi api;
	private final String showID;

	public PodcastListener(String showID, YamlFile config, SpotifyApi api) {
		this.config = config;
		this.api = api;
		this.showID = showID;

		run();
		Main.scheduleDaily(12, this);
	}

	@Override
	public void run() {
		List<String> publishedEpisodes = config.getStringList("show." + showID + ".publishedEpisodes");

		for(EpisodeSimplified episode : getLatestEpisodes()) {
			if(!publishedEpisodes.contains(episode.getId())) {
				publishedEpisodes.add(episode.getId());
				broadcastEpisode(episode);
			}
		}

		config.set("show." + showID + ".publishedEpisodes", publishedEpisodes);

		try {
			config.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private EpisodeSimplified[] getLatestEpisodes() {
		SpotifyListener.logger.info("Überprüfe auf neue Episoden bei {}", showID);

		try {
			Paging<EpisodeSimplified> episodes = api.getShowEpisodes(showID).market(CountryCode.DE).limit(20).build().execute();

			if(episodes.getTotal() > 20) {
				SpotifyListener.logger.warn("Es gibt mehr als 20 Episoden, hole die letzten 20");
				episodes = api.getShowEpisodes(showID).market(CountryCode.DE).offset(episodes.getTotal() - 20).build().execute();
			}

			List<EpisodeSimplified> episodeList = new ArrayList<>(Arrays.asList(episodes.getItems()));

			return episodeList.toArray(EpisodeSimplified[]::new);
		} catch(IOException | SpotifyWebApiException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void broadcastEpisode(EpisodeSimplified episode) {
		String message = MessageFormat.format(config.getString("show." + showID + ".message"), episode.getName(), episode.getExternalUrls().get("spotify"));

		TextChannel channel = Main.jdaInstance.getTextChannelById(config.getLong("show." + showID + ".channelId"));

		if(channel == null) throw new RuntimeException("Channel not found");

		channel.sendMessage(message).queue();
	}
}
