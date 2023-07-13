package com.slimebot.alerts.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpotifyListener implements Runnable {
	public final static Logger logger = LoggerFactory.getLogger(SpotifyListener.class);

	private final String artistId;
	private final long channelId;
	private final SpotifyApi spotifyApi;
	private final String message;

	private final ConfigurationSection section;

	private final YamlFile config;

	private final List<String> publishedAlbums;

	public SpotifyListener(String artistId, YamlFile config, String message, SpotifyApi api) {
		this.config = config;
		this.section = config.getConfigurationSection("artists." + artistId);
		this.artistId = artistId;
		this.channelId = section.getLong("channelId");
		this.message = message;
		this.spotifyApi = api;
		this.publishedAlbums = section.getStringList("publishedAlbums");

		run();
		Main.scheduleDaily(12, this);
	}

	public void run() {
		logger.info("Überprüfe auf neue Releases");

		for(AlbumSimplified album : getLatestAlbums()) {
			if(!publishedAlbums.contains(album.getId())) {
				logger.info("Album {} wurde veröffentlicht", album.getName());
				publishedAlbums.add(album.getId());
				broadcastAlbum(album);
			}
		}

		try {
			config.set("artists." + artistId + ".publishedAlbums", publishedAlbums);
			config.save();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private AlbumSimplified[] getLatestAlbums() {
		GetArtistsAlbumsRequest request = spotifyApi.getArtistsAlbums(artistId).market(CountryCode.DE).limit(20).build();
		Paging<AlbumSimplified> albumSimplifiedPaging;

		try {
			albumSimplifiedPaging = request.execute();
			if(albumSimplifiedPaging.getTotal() > 20) {
				logger.warn("Es wurden mehr als 20 Alben gefunden. Es werden nur die 20 neuesten veröffentlicht");
				albumSimplifiedPaging = spotifyApi.getArtistsAlbums(artistId).market(CountryCode.DE).limit(20).offset(albumSimplifiedPaging.getTotal() - 20).build().execute();
			}

			List<AlbumSimplified> albums = Arrays.asList(albumSimplifiedPaging.getItems());
			Collections.reverse(albums);

			return albums.toArray(new AlbumSimplified[0]);
		} catch(Exception e) {
			logger.error("Alben können nicht geladen werden");
			throw new RuntimeException(e);
		}
	}

	private void broadcastAlbum(AlbumSimplified album) {
		MessageChannel channel = Main.jdaInstance.getChannelById(MessageChannel.class, channelId);

		if(channel == null) throw new RuntimeException("Channel not found");

		channel.sendMessage(MessageFormat.format(message, album.getName(), album.getExternalUrls().get("spotify"))).queue();
	}

}
