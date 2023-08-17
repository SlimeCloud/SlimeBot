package com.slimebot.alerts.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.main.config.guild.SpotifyNotificationConfig;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.hc.core5.http.ParseException;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.data.AbstractDataPagingRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class SpotifyListener {
	private long tokenExpiry = 0;

	private final SpotifyApi api;

	public SpotifyListener() {
		this.api = new SpotifyApi.Builder()
				.setClientId(Main.config.spotify.clientId)
				.setClientSecret(Main.config.spotify.clientSecret)
				.build();
	}
	public void register() {
		Main.scheduleAtFixedRate(1, TimeUnit.HOURS, this::check);
	}


	public void check() {
		if(System.currentTimeMillis() > tokenExpiry) fetchToken();
		
		List<String> known = Main.config.database != null
				? Main.database.handle(handle -> handle.createQuery("select id from spotify_known").mapTo(String.class).list())
				: Collections.emptyList();

		List<String> newIds = new ArrayList<>();

		logger.info("Überprüfe auf neue Podcast Folgen...");

		Main.config.spotify.podcast.artistIds.stream()
				.flatMap(id -> getLatestEntries(id, api::getShowEpisodes).stream())
				.filter(e -> !known.contains(e.getId()))
				.forEach(e -> {
					newIds.add(e.getId());
					broadcast(Main.config.spotify.podcast.message, SpotifyNotificationConfig::getPodcastChannel, e.getName(), e.getExternalUrls().get("spotify"));
				});

		logger.info("Überprüfe auf neue Musik Releases...");

		Main.config.spotify.music.artistIds.stream()
				.flatMap(id -> getLatestEntries(id, api::getArtistsAlbums).stream())
				.filter(e -> !known.contains(e.getId()))
				.forEach(e -> {
					newIds.add(e.getId());
					broadcast(Main.config.spotify.music.message, SpotifyNotificationConfig::getMusicChannel, e.getName(), e.getExternalUrls().get("spotify"));
				});

		Main.database.run(handle -> {
			PreparedBatch update = handle.prepareBatch("insert into spotify_known values(:id)");

			newIds.forEach(id -> update.bind("id", id).add());

			update.execute();
		});
	}

	private <T, R extends AbstractDataPagingRequest.Builder<T, ?>> List<T> getLatestEntries(String id, Function<String, R> request) {
		logger.info("Überprüfe auf Einträge bei {}...", id);

		try {
			Paging<T> albumSimplifiedPaging = request.apply(id).setQueryParameter("market", CountryCode.DE).limit(20).build().execute();

			if (albumSimplifiedPaging.getTotal() > 20) {
				logger.warn("Es wurden mehr als 20 Einträge gefunden. Es werden nur die 20 neuesten veröffentlicht");
				albumSimplifiedPaging = request.apply(id).setQueryParameter("market", CountryCode.DE).limit(20).offset(albumSimplifiedPaging.getTotal() - 20).build().execute();
			}

			List<T> albums = Arrays.asList(albumSimplifiedPaging.getItems());
			logger.info("{} Einträge gefunden", albums.size());
			Collections.reverse(albums);
			return albums;
		} catch (Exception e) {
			logger.error("Einträge können nicht geladen werden", e);
			return Collections.emptyList();
		}
	}

	private void broadcast(String format, Function<SpotifyNotificationConfig, Optional<GuildMessageChannel>> channel, String name, String url) {
		for (Guild guild : Main.jdaInstance.getGuilds()) {
			GuildConfig.getConfig(guild).getSpotify().ifPresent(spotify ->
					channel.apply(spotify).ifPresent(ch -> {
						String notification = spotify.getRole()
								.map(Role::getAsMention)
								.orElse("");

						ch.sendMessage(format
								.replace("%notification%", notification)
								.replace("%name%", name)
								.replace("%url%", url)
						).queue();
					})
			);
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	private void fetchToken(){
		try {
			ClientCredentials credentials = api.clientCredentials().build().execute();
			api.setAccessToken(credentials.getAccessToken());
			tokenExpiry = System.currentTimeMillis() + credentials.getExpiresIn() * 1000;
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			logger.error("Spotify login fehlgeschlagen", e);
		}
	}
}
