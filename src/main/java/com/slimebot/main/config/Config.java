package com.slimebot.main.config;

import com.slimebot.main.Database;
import com.slimebot.main.Main;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Config {
	public final static Dotenv env = Dotenv.load();
	public Activity activity;


	public DatabaseConfig database;
	public SpotifyConfig spotify;
	public GitHubConfig github;

	public String color;

	public static Config readFromFile(String file) throws IOException {
		try(Reader reader = new FileReader(file)) {
			Config config = Main.gson.fromJson(reader, Config.class);

			if(config.activity == null || config.color == null) {
				throw new IOException("Notwendiges Konfigurationsfeld nicht gesetzt. Siehe https://github.com/SlimeCloud/java-SlimeBot/blob/master/config_preset");
			}

			if(config.database == null) {
				Database.logger.warn("Keine Datenbank konfiguriert. Einige Funktionen werden nicht verfügbar sein!");
			}

			return config;
		}
	}

	public static class Activity {
		public ActivityType type;
		public String text;

		public net.dv8tion.jda.api.entities.Activity build() {
			return net.dv8tion.jda.api.entities.Activity.of(type, text);
		}
	}
}
