package com.slimebot.main.config.guild;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.main.Main;
import com.slimebot.main.config.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Diese Klasse wird verwendet, um die Konfiguration eines Servers zu verwalten.
 * Alle Variablen in dieser Klasse haben Getter-Methoden, die ein {@link Optional} zurückgeben. Dadurch wird der handhaben von nicht gesetzten Konfigurationsfeldern vereinfacht.
 * Wenn du hier eigene Felder hinzufügst, solltest du ebenfalls einen entsprechenden Getter hinzufügen.
 * Falls das Feld ein Kanal- oder Rollenid ist, kannst du für deinen Getter {@link #getChannel(Long)} oder {@link #getRole(Long)} verwenden. Siehe dir dazu an, wie dies bei bereits bestehenden Feldern gemacht wurde.
 * <p>
 * Alle Konfigurationsfelder sollten mit den {@link ConfigCommand} (zurück-) setzbar sein. Wenn du also ein Konfigurationsfeld hinzufügst, solltest du auch einen dazugehörigen Unterbefehl erstellen.
 */
public class GuildConfig {
	public static final Logger logger = LoggerFactory.getLogger(GuildConfig.class);

	private static final Map<Long, GuildConfig> guildConfig = new HashMap<>();

	/**
	 * VERWENDE NICHT DIESE METHODE!
	 * Die Konfiguration für Server wird automatisch geladen.
	 * Um auf sie zuzugreifen, verwende die {@link #getConfig(Guild)}-Methode.
	 * @see #getConfig(Guild) 
	 */
	public static void load(Guild guild) {
		try {
			File file = new File("guild/" + guild.getIdLong() + ".json");

			if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			try(Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
				GuildConfig config = Main.gson.fromJson(reader, GuildConfig.class);

				if(config == null) {
					config = new GuildConfig();
				}

				config.guild = guild.getIdLong();
				guildConfig.put(guild.getIdLong(), config);
			}
		} catch(Exception e) {
			logger.error("Failed to load config for guild " + guild, e);
		}
	}

	/**
	 * @param guild Ein Server
	 * @return Die Konfiguration dieses Servers
	 */
	public static GuildConfig getConfig(Guild guild) {
		return guildConfig.get(guild.getIdLong());
	}

	/**
	 * @param guild Ein Server
	 * @return Die Farbe, die für diesen Server konfiguriert ist. Falls der Server {@code null} ist oder für ihn keine Farbe konfiguriert ist, wird {@link Config#color} zurückgegeben.
	 */
	public static Color getColor(Guild guild) {
		return Optional.ofNullable(guild).map(GuildConfig::getConfig).flatMap(GuildConfig::getColor).orElse(Color.decode(Main.config.color));
	}

	/**
	 * @param guild Die ID eines Servers
	 * @return Die Konfiguration für diesen Server
	 */
	public static GuildConfig getConfig(long guild) {
		return guildConfig.get(guild);
	}

	/**
	 * @param guild Die ID eines Servers
	 * @return Die Farbe, die für diesen Server konfiguriert ist. Falls für den Server keine Farbe konfiguriert ist, wird {@link Config#color} zurückgegeben.
	 */
	public static Color getColor(long guild) {
		return getConfig(guild).getColor().orElse(Color.decode(Main.config.color));
	}

	/**
	 * Speichert die Konfiguration in der Datei.
	 * Diese Methode sollte vermieden werden.
	 * Wenn du im {@link ConfigCommand} die konfiguration veränderst, nutze {@link ConfigCommand#updateField(Guild, Consumer)}
	 */
	public synchronized void save() {
		try(Writer writer = new FileWriter("guild/" + guild + ".json", StandardCharsets.UTF_8)) {
			Main.gson.toJson(this, writer);
		} catch(Exception e) {
			logger.error("Failed to save config for guild " + guild, e);
		}
	}

	private transient long guild;

	public String color;

	public Long logChannel;
	public Long greetingsChannel;
	public Long punishmentChannel;

	public Long contributorRole;

	public Long staffRole;

	public SpotifyNotificationConfig spotify;
	public FdmdsConfig fdmds;
	public StaffConfig staffMessage;
	public LevelGuildConfig level;

	public Optional<Color> getColor() {
		return Optional.ofNullable(color).map(Color::decode);
	}

	public Optional<GuildMessageChannel> getLogChannel() {
		return getChannel(logChannel);
	}

	public Optional<GuildMessageChannel> getGreetingsChannel() {
		return getChannel(greetingsChannel);
	}

	public Optional<GuildMessageChannel> getPunishmentChannel() {
		return getChannel(punishmentChannel);
	}


	public Optional<Role> getContributorRole() {
		return getRole(contributorRole);
	}

	public Optional<Role> getStaffRole() {
		return getRole(staffRole);
	}


	public Optional<FdmdsConfig> getFdmds() {
		return Optional.ofNullable(fdmds);
	}

	public FdmdsConfig getOrCreateFdmds() {
		return getFdmds().orElseGet(() -> fdmds = new FdmdsConfig());
	}

	public Optional<SpotifyNotificationConfig> getSpotify() {
		return Optional.ofNullable(spotify);
	}

	public SpotifyNotificationConfig getOrCreateSpotify() {
		return getSpotify().orElseGet(() -> spotify = new SpotifyNotificationConfig());
	}

	public Optional<StaffConfig> getStaffConfig() {
		return Optional.ofNullable(staffMessage);
	}

	public StaffConfig getOrCreateStaff() {
		return getStaffConfig().orElseGet(() -> staffMessage = new StaffConfig());
	}

	public Optional<LevelGuildConfig> getLevelConfig() {
		return Optional.ofNullable(level);
	}

	public LevelGuildConfig getOrCreateLevel() {
		return getLevelConfig().orElseGet(() -> level = new LevelGuildConfig());
	}

	//Internal helper methods
	static Optional<GuildMessageChannel> getChannel(Long channel) {
		return Optional.ofNullable(channel).map(id -> Main.jdaInstance.getChannelById(GuildMessageChannel.class, id));
	}

	static Optional<Role> getRole(Long role) {
		return Optional.ofNullable(role).map(id -> Main.jdaInstance.getRoleById(id));
	}
}
