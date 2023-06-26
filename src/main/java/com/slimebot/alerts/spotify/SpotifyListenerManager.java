package com.slimebot.alerts.spotify;

import org.apache.hc.core5.http.ParseException;
import org.simpleyaml.configuration.file.YamlFile;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Map;

public class SpotifyListenerManager {
    final YamlFile config = new YamlFile("Slimebot/spotify/config.yml");

    public SpotifyListenerManager() {
        if (!config.exists()) {
            try {
                config.createNewFile();
                createConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SpotifyApi api = new SpotifyApi.Builder()
                .setClientId(config.getString("clientId"))
                .setClientSecret(config.getString("clientSecret"))
                .build();
        try {
            api.setAccessToken(api.clientCredentials().build().execute().getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
        String message = config.getString("message");
        for (Map.Entry<String, Object> entry : config.getConfigurationSection("artists").getMapValues(false).entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                System.out.println("[SPOTIFY] Error in config.yml: " + entry.getKey() + " is not a String");
                continue;
            }
            String artistID = entry.getKey();
            long channelID = (Integer) entry.getValue();
            new SpotifyListener(artistID, channelID, message, api);
            System.out.println("[SPOTIFY] Added " + artistID + " to the Channel " + channelID);
        }
    }

    private void createConfig() {
        config.set("clientId", "");
        config.set("clientSecret", "");
        config.set("message", """
                <@&roleId>
                            
                ## 😌ACHTUNG ACHTUNG😌
                Deine Playlist ist voll öde??
                Du suchst neue Musik??
                Du willst mal was anderes? Was richtig, richtig cooles? Was nur die krassen Kids hören?? :P
                            
                Dann gönn dir umbedingt das neue Album: **{0}**
                            
                {1}
                """);
        config.setComment("message", "Format: {0} = Albumname, {1} = Link zum Album");
        config.set("artists.xvjasildjf", 123456);
        config.setComment("artists", "Format: <artist id>: <channel id>");
        try {
            config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
