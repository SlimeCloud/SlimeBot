package com.slimebot.alerts.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.slimebot.main.DatabaseField;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.EpisodeSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class PodcastListener implements Runnable {
	private final SpotifyApi api;
	private final String id;

	public PodcastListener(SpotifyApi api, String id) {
		this.api = api;
		this.id = id;

		run();
		Main.scheduleDaily(12, this);
	}

	@Override
	public void run() {
		SpotifyListener.logger.info("Überprüfe auf neue Podcast Episode");

		List<String> known = Main.database.handle(handle -> handle.createQuery("select id from spotify_known").mapTo(String.class).list());

		for(EpisodeSimplified episode : getLatestEpisodes()) {
			if(known.contains(episode.getId())) continue;

			broadcastEpisode(episode);
		}
	}

	private List<EpisodeSimplified> getLatestEpisodes() {
		SpotifyListener.logger.info("Überprüfe auf neue Episoden bei {}", id);

		try {
			Paging<EpisodeSimplified> episodes = api.getShowEpisodes(id).market(CountryCode.DE).limit(20).build().execute();

			if(episodes.getTotal() > 20) {
				SpotifyListener.logger.warn("Es gibt mehr als 20 Episoden, hole die letzten 20");
				episodes = api.getShowEpisodes(id).market(CountryCode.DE).offset(episodes.getTotal() - 20).build().execute();
			}

			return Arrays.asList(episodes.getItems());
		} catch(IOException | SpotifyWebApiException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void broadcastEpisode(EpisodeSimplified episode) {
		for(Guild guild : Main.jdaInstance.getGuilds()) {
			MessageChannel channel = Main.database.getChannel(guild, DatabaseField.SPOTIFY_PODCAST_CHANNEL);

			if(channel == null) {
				SpotifyListener.logger.warn("Kanal nicht verfügbar");
				continue;
			}

			channel.sendMessage(MessageFormat.format(Main.config.spotify.music.message,
					Main.database.getRole(guild, DatabaseField.SPOTIFY_NOTIFICATION_ROLE),
					episode.getName(),
					episode.getExternalUrls().get("spotify")
			)).queue();
		}
	}
}
