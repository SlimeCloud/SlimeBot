package com.slimebot.alerts.spotify;

import com.slimebot.main.Main;
import com.slimebot.utils.DailyTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ReleaseDatePrecision;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class SpotifyListener implements Runnable {
    private final String artistId;
    private final long channelId;
    private final SpotifyApi spotifyApi;
    private final String message;

    SpotifyListener(String artistId, long channelId, String message, SpotifyApi api) {
        this.artistId = artistId;
        this.channelId = channelId;
        this.message = message;
        spotifyApi = api;
        new DailyTask(12, this);
    }

    public void run() {
        log("INFO: Überprüfe auf neue Releases");
        try {
            AlbumSimplified latestAlbum = getLatestAlbum();
            if (!isPublishedToday(latestAlbum)) {
                log("INFO: Kein neuer Release");
                return;
            }
            JDA jda = Main.getJDAInstance();
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                log("ERROR: Channel nicht verfügbar: " + channelId);
                return;
            }
            channel.sendMessage(MessageFormat.format(message, latestAlbum.getName(), latestAlbum.getExternalUrls().get("spotify"))).queue();
        } catch (Exception e) {
            log("ERROR: Release des Albums kann nicht überprüft werden");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private AlbumSimplified getLatestAlbum() {
        GetArtistsAlbumsRequest request = spotifyApi.getArtistsAlbums(artistId).build();
        final Paging<AlbumSimplified> albumSimplifiedPaging;
        try {
            albumSimplifiedPaging = request.execute();
            return albumSimplifiedPaging.getItems()[0];
        } catch (Exception e) {
            log("ERROR: Alben können nicht geladen werden");
            throw new RuntimeException(e);
        }
    }

    private void log(String s) {
        System.out.println("[SPOTIFY] " + s);
    }

    private boolean isPublishedToday(AlbumSimplified album) {
        if (album.getReleaseDatePrecision() != ReleaseDatePrecision.DAY) {
            log("Album kann nicht geladen werden: zu ungenaue release Angabe " + album.getReleaseDatePrecision().precision);
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            LocalDate date = format.parse(album.getReleaseDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate now = LocalDate.now();
            return now.isEqual(date);
        } catch (ParseException e) {
            log("Album kann nicht geladen werden: Falsches Release-Date Format: " + album.getReleaseDate());
            return false;
        }
    }
}
