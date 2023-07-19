package com.slimebot.alerts.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.apache.hc.core5.http.ParseException;
import org.jdbi.v3.core.statement.PreparedBatch;
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

		Main.database.run(handle -> {
			List<String> known = handle.createQuery("select id from spotify_known").mapTo(String.class).list();
			PreparedBatch update = handle.prepareBatch("insert into spotify_known values(:id)");

			for(EpisodeSimplified episode : getLatestEpisodes()) {
				if(known.contains(episode.getId())) continue;

				broadcastEpisode(episode);
				update.bind("id", episode.getId()).add();
			}

			update.execute();
		});
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
			GuildConfig.getConfig(guild).getSpotify().ifPresent(spotify ->
					spotify.getPodcastChannel().ifPresent(channel -> {
						String notification = spotify.getRole()
								.map(Role::getAsMention)
								.orElse("");

						channel.sendMessage(MessageFormat.format(Main.config.spotify.podcast.message,
								notification,
								episode.getName(),
								episode.getExternalUrls().get("spotify")
						)).queue();
					})
			);
		}
	}
}
