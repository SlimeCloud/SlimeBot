package com.slimebot.alerts.spotify;

import com.slimebot.main.Main;
import com.slimebot.utils.DailyTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
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
    private final String artistId;
    private final long channelId;
    private final SpotifyApi spotifyApi;
    private final String message;

    private final ConfigurationSection section;

    private final YamlFile config;

    private final List<String> publishedAlbums;


    SpotifyListener(String artistId, YamlFile config, String message, SpotifyApi api) {
        this.config = config;
        this.section = config.getConfigurationSection("artists." + artistId);
        this.artistId = artistId;
        this.channelId = section.getLong("channelId");
        this.message = message;
        spotifyApi = api;
        publishedAlbums = section.getStringList("publishedAlbums");
        try {
            Main.getJDAInstance().awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        run();
        new DailyTask(12, this);
    }

    public void run() {
        log("INFO: Überprüfe auf neue Releases");
        for (AlbumSimplified album : getLatestAlbums()) {
            if (!publishedAlbums.contains(album.getId())) {
                log("INFO: Album " + album.getName() + " wurde veröffentlicht");
                publishedAlbums.add(album.getId());
                broadcastAlbum(album);
            }

        }
        try {
            config.set("artists." + artistId + ".publishedAlbums", publishedAlbums);
            config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AlbumSimplified[] getLatestAlbums() {
        GetArtistsAlbumsRequest request = spotifyApi.getArtistsAlbums(artistId).build();
        final Paging<AlbumSimplified> albumSimplifiedPaging;
        try {
            albumSimplifiedPaging = request.execute();
            List<AlbumSimplified> albums = Arrays.asList(albumSimplifiedPaging.getItems());
            Collections.reverse(albums);
            return albums.toArray(new AlbumSimplified[0]);
        } catch (Exception e) {
            log("ERROR: Alben können nicht geladen werden");
            throw new RuntimeException(e);
        }
    }

    private void log(String s) {
        System.out.println("[SPOTIFY] " + s);
    }

    private void broadcastAlbum(AlbumSimplified album) {
        JDA jda = Main.getJDAInstance();
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            log("ERROR: Channel nicht verfügbar: " + channelId);
            return;
        }
        channel.sendMessage(MessageFormat.format(message, album.getName(), album.getExternalUrls().get("spotify"))).queue();
    }
}
