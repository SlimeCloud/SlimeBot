package com.slimebot.alerts.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpotifyListener implements Runnable {
	public final static Logger logger = LoggerFactory.getLogger(SpotifyListener.class);

	private final String artistId;
	private final SpotifyApi spotifyApi;

	public SpotifyListener(SpotifyApi api, String artistId) {
		this.spotifyApi = api;
		this.artistId = artistId;

		run();
		Main.scheduleDaily(12, this);
	}

	public void run() {
		logger.info("Überprüfe auf neue Releases");

		Main.database.run(handle -> {
			List<String> known = handle.createQuery("select id from spotify_known").mapTo(String.class).list();
			PreparedBatch update = handle.prepareBatch("insert into spotify_known values(:id)");

			for(AlbumSimplified album : getLatestAlbums()) {
				if(known.contains(album.getId())) continue;

				broadcastAlbum(album);
				update.bind("id", album.getId()).add();
			}

			update.execute();
		});
	}

	private List<AlbumSimplified> getLatestAlbums() {
		try {
			Paging<AlbumSimplified> albumSimplifiedPaging = spotifyApi.getArtistsAlbums(artistId).market(CountryCode.DE).limit(20).build().execute();

			if(albumSimplifiedPaging.getTotal() > 20) {
				logger.warn("Es wurden mehr als 20 Alben gefunden. Es werden nur die 20 neuesten veröffentlicht");
				albumSimplifiedPaging = spotifyApi.getArtistsAlbums(artistId).market(CountryCode.DE).limit(20).offset(albumSimplifiedPaging.getTotal() - 20).build().execute();
			}

			List<AlbumSimplified> albums = Arrays.asList(albumSimplifiedPaging.getItems());
			Collections.reverse(albums);
			return albums;
		} catch(Exception e) {
			logger.error("Alben können nicht geladen werden");
			throw new RuntimeException(e);
		}
	}

	private void broadcastAlbum(AlbumSimplified album) {
		for(Guild guild : Main.jdaInstance.getGuilds()) {
			GuildConfig.getConfig(guild).getSpotify().ifPresent(spotify ->
					spotify.getMusicChannel().ifPresent(channel -> {
						String notification = spotify.getRole()
								.map(Role::getAsMention)
								.orElse("");

						channel.sendMessage(MessageFormat.format(Main.config.spotify.music.message,
								notification,
								album.getName(),
								album.getExternalUrls().get("spotify")
						)).queue();
					})
			);
		}
	}
}
