package com.slimebot.main.config;

import com.slimebot.main.Database;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Wenn du Konfiguration für den Bot allgemein benötigst, kann du hier Variablen hinzufügen. Sie werden dann automatisch aus der `config`-Datei gelesen.
 * Wenn du Server spezifische Konfiguration benötigst, verende entweder die `GuildConfig` oder eine Datenbank Tabelle.
 * @see Main#config
 * @see GuildConfig
 */
public class Config {
	public final static Dotenv env = Dotenv.load();

	public Activity activity;


	public DatabaseConfig database;
	public SpotifyConfig spotify;
	public GitHubConfig github;

	public String color;

	/**
	 * VERWENDE NICHT DIESE METHODE!
	 * Doe Konfiguration wird bereits beim Starten gelesen und ist in der {@link Main}-Klasse verfügbar.
	 * @see Main#config
	 */
	public static Config readFromFile(String file) throws IOException {
		try(Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
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
