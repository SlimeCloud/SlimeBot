package com.slimebot.main.config;

import java.util.List;

public class SpotifyConfig {
    public String clientId;
    public String clientSecret;

    public SpotifyNotification music;
    public SpotifyNotification podcast;

    public static class SpotifyNotification {
        public String message;
        public List<String> artistIds;
    }

}
