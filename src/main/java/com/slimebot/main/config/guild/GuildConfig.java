package com.slimebot.main.config.guild;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.commands.config.FdmdsConfigCommand;
import com.slimebot.commands.config.LevelConfigCommand;
import com.slimebot.commands.config.StaffConfigCommand;
import com.slimebot.commands.config.engine.ConfigCategory;
import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import com.slimebot.commands.config.engine.FieldVerification;
import com.slimebot.commands.config.setup.AutoDeleteFrame;
import com.slimebot.commands.config.setup.StaffFrame;
import com.slimebot.main.Main;
import com.slimebot.main.config.Config;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Diese Klasse wird verwendet, um die Konfiguration eines Servers zu verwalten.
 * Alle Variablen in dieser Klasse haben Getter-Methoden, die ein {@link Optional} zurückgeben. Dadurch wird der handhaben von nicht gesetzten Konfigurationsfeldern vereinfacht.
 * Wenn du hier eigene Felder hinzufügst, solltest du ebenfalls einen entsprechenden Getter hinzufügen.
 * Falls das Feld ein Kanal- oder Rollenid ist, kannst du für deinen Getter {@link #getChannel(Long)} oder {@link #getRole(Long)} verwenden. Siehe dir dazu an, wie dies bei bereits bestehenden Feldern gemacht wurde.
 * <p>
 * Damit unser system automatisch Konfigurationsbefehle für die Felder erstellen kann, müssen Kategorien mit {@link ConfigCategory} und Felder mit {@link ConfigField} annotiert sein.
 * Anhand der bereits vorhandenden Beispiele sollte erkennbar sein, wie diese Annotationen zu verwenden sind.
 */
@ConfigCategory(name = "guild", description = "Haupteinstellungen")
@Slf4j
public class GuildConfig {

	private static final Map<Long, GuildConfig> guildConfig = new HashMap<>();

	/**
	 * VERWENDE NICHT DIESE METHODE!
	 * Die Konfiguration für Server wird automatisch geladen.
	 * Um auf sie zuzugreifen, verwende die {@link #getConfig(Guild)}-Methode.
	 *
	 * @see #getConfig(Guild)
	 */
	public static void load(Guild guild) {
		try {
			File file = new File("guild/" + guild.getIdLong() + ".json");

			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			@Cleanup
			Reader reader = new FileReader(file, StandardCharsets.UTF_8);
			GuildConfig config = Main.gson.fromJson(reader, GuildConfig.class);

			if (config == null) {
				config = new GuildConfig();
			}

			config.guild = guild.getIdLong();
			guildConfig.put(guild.getIdLong(), config);

		} catch (Exception e) {
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
	 * Wenn du im {@link ConfigCommand} die konfiguration veränderst, nutze {@link ConfigCommand#updateField}
	 */
	public synchronized void save() {
		try (Writer writer = new FileWriter("guild/" + guild + ".json", StandardCharsets.UTF_8)) {
			Main.gson.toJson(this, writer);
		} catch (Exception e) {
			logger.error("Failed to save config for guild " + guild, e);
		}
	}

	private transient long guild;

	@ConfigField(type = ConfigFieldType.STRING, command = "color", title = "\uD83C\uDFA8 Farbe", description = "Die Farbe, die für Embeds verwendet wird", verifier = FieldVerification.COLOR)
	public String color;

	@ConfigField(type = ConfigFieldType.CHANNEL, command = "log_channel", title = "Log-Kanal", description = "In diesem Kanal werden Informationen bezüglich des Bots gesendet")
	public Long logChannel;

	@ConfigField(type = ConfigFieldType.CHANNEL, command = "greetings_channel", title = "Gruß-Kanal", description = "In diesem Kanal werden Gruß-Nachrichten - wie z.B. zu Ferien-Beginnen - gesendet")
	public Long greetingsChannel;

	@ConfigField(type = ConfigFieldType.CHANNEL, command = "punishment_channel", title = "Straf-Kanal", description = "In diesem Kanal werden Informationen über Bestrafungen gesendet")
	public Long punishmentChannel;

	@ConfigField(type = ConfigFieldType.ROLE, command = "contributor_role", title = "Contributor Rolle", description = "Diese Rollen können Mitglieder beantragen, die an diesem Bot auf GitHub mitgearbeitet haben")
	public Long contributorRole;

	@ConfigField(type = ConfigFieldType.ROLE, command = "staff_role", title = "Team Rolle", description = "Diese Rolle hat Zugang zu beschränkten Befehlen")
	public Long staffRole;

	@ConfigCategory(name = "spotify", description = "Spotify Benachrichtigungen")
	public SpotifyNotificationConfig spotify;

	@ConfigCategory(name = "fdmds", description = "Frag doch mal den Schleim", updateCommands = true,
			subcommands = FdmdsConfigCommand.class
	)
	public FdmdsConfig fdmds;

	@ConfigCategory(name = "staff", description = "Team-Nachricht",
			subcommands = StaffConfigCommand.class,
			customFrames = {StaffFrame.StaffChannelFrame.class, StaffFrame.StaffRolesFrame.class}
	)
	public StaffConfig staffMessage;

	@ConfigCategory(name = "level", description = "Level-System", updateCommands = true,
			subcommands = LevelConfigCommand.class
	)
	public LevelGuildConfig level;

	@ConfigCategory(name = "assignrole", description = "Join Role")
	public AssignRoleConfig assignRole;

	@ConfigCategory(name = "quote", description = "Zitate", updateCommands = true)
	public QuoteConfig quote;

	@ConfigCategory(name = "auto-delete", description = "Automatisches Nachrichtenlöschen", customFrames = AutoDeleteFrame.class)
	public AutoDeleteConfig autoDelete;

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

	public Optional<SpotifyNotificationConfig> getSpotify() {
		return Optional.ofNullable(spotify);
	}

	public Optional<StaffConfig> getStaffConfig() {
		return Optional.ofNullable(staffMessage);
	}

	public Optional<LevelGuildConfig> getLevelConfig() {
		return Optional.ofNullable(level);
	}

	public Optional<AssignRoleConfig> getAssignRole() {
		return Optional.ofNullable(assignRole);
	}

	public Optional<QuoteConfig> getQuoteConfig() {
		return Optional.ofNullable(quote);
	}

	public Optional<AutoDeleteConfig> getAutoDeleteConfig() {
		return Optional.ofNullable(autoDelete);
	}

	public StaffConfig getOrCreateStaff() {
		return getStaffConfig().orElseGet(() -> staffMessage = new StaffConfig());
	}

	public LevelGuildConfig getOrCreateLevel() {
		return getLevelConfig().orElseGet(() -> level = new LevelGuildConfig());
	}

	public AutoDeleteConfig getOrCreateAutoDelete() {
		return getAutoDeleteConfig().orElseGet(() -> autoDelete = new AutoDeleteConfig());
	}

	//Internal helper methods
	static Optional<GuildMessageChannel> getChannel(Long channel) {
		return getChannel(channel, GuildMessageChannel.class);
	}

	static Optional<GuildMessageChannel> getChannel(String channel) {
		return getChannel(channel, GuildMessageChannel.class);
	}

	static <T extends GuildChannel> Optional<T> getChannel(Long channel, Class<T> type) {
		return Optional.ofNullable(channel).map(id -> Main.jdaInstance.getChannelById(type, id));
	}

	static <T extends GuildChannel> Optional<T> getChannel(String channel, Class<T> type) {
		return Optional.ofNullable(channel).map(id -> Main.jdaInstance.getChannelById(type, id));
	}

	static Optional<Role> getRole(Long role) {
		return Optional.ofNullable(role).map(id -> Main.jdaInstance.getRoleById(id));
	}

	static Optional<List<Role>> getRoles(List<Long> roles) {
		return Optional.ofNullable(roles).map(list -> list.stream().map(Main.jdaInstance::getRoleById).toList());
	}

	static <T extends Channel> Optional<List<T>> getChannels(List<Long> channels, Class<T> type) {
		return Optional.ofNullable(channels).map(list -> list.stream().map(id -> Main.jdaInstance.getChannelById(type, id)).toList());
	}

}
